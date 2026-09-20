# Validation Engine

Validation Engine is a Java 25 Gradle project for streaming JSON records through a catalog of validation rules. It contains reusable library modules and a small HTTP application.

## Modules

- `core` - framework-free validation models and engine.
- `rule-catalog` - built-in validation rules.
- `json-adapter` - Jackson JSON reader and validator adapter.
- `engine` - public `Validators` factory, published as the `validation-engine` library.
- `api` - public result models and validator contract.
- `test-agent` - runnable HTTP API using the library.

## Requirements

- JDK 25 (`java --version`)
- Gradle Wrapper (`./gradlew`)

## 1. Compile the application

From the project root:

```bash
./gradlew clean build
```

This compiles all modules and runs their tests.

## 2. Run from IntelliJ IDEA

Open the project directory as a Gradle project and select JDK 25 as the Gradle JVM. Open `ValidationApplication.java` in the `test-agent` module and run its `main` method. The server starts on:

```text
http://localhost:8080
```

The application listens on `localhost:8080`.

## 3. Run from the command line

```bash
./gradlew :test-agent:run
```

The application listens on `localhost:8080`.

## 4. Validate a JSON file with curl

The file must contain a JSON array of records. The request body is streamed by the server.

```bash
curl -i \
  -X POST http://localhost:8080/validate \
  -H 'Content-Type: application/json' \
  --data-binary @records.json
```

The response contains a validation summary. It also includes a `Validation-Id` response header, which identifies the saved results and errors.

## 5. Validate JSON supplied in the request body

```bash
curl -i \
  -X POST http://localhost:8080/validate \
  -H 'Content-Type: application/json' \
  --data '[{"id":"r1","vatId":"DE111111111","country":"DE","iban":"DE123456789"}]'
```

## 6. Read and remember the validation ID

The `/validate` response includes a `Validation-Id` header. Read and remember its value because it is required to retrieve the rule results and errors later:

The header will look like this:

```text
Validation-Id: 123456789
```

Use that value as `<Validation-Id>` in the following requests.

## 7. Retrieve rule results

Replace `<Validation-Id>` with the value returned by `/validate`:

```bash
curl -sS \
  -o rule-results.json \
  http://localhost:8080/validation/<Validation-Id>/results
```

The downloaded `rule-results.json` file is a JSON array containing the result produced by each successful rule evaluation.

## 8. Retrieve rule errors

```bash
curl -sS \
  -o rule-errors.json \
  http://localhost:8080/validation/<Validation-Id>/errors
```

The downloaded `rule-errors.json` file is a JSON array containing rule failures. Each error includes the record ID, rule ID, short error type, message, and fields read before the failure.

## 9. Use the library from another Gradle project

The public library surface is the factory in `engine` plus the contracts in `api`. The JSON adapter, core and rule catalog are implementation dependencies. Publish all library modules to the local Maven repository:

```bash
./gradlew publishToMavenLocal
```

In the consuming project, add `mavenLocal()` and the JSON adapter dependency:

```groovy
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation 'com.cdq.validationengine:validation-engine:0.1.0-SNAPSHOT'
    // Required by the output serialization example below.
    implementation 'tools.jackson.core:jackson-databind:3.2.0'
}
```

Then use the library directly. The input is consumed record by record from the input stream. Results and errors can be written incrementally to output streams, so the complete request or complete result set does not need to be held in memory:

```java
var mapper = JsonMapper.builder().build();

try(
InputStream input = Files.newInputStream(Path.of("records.json"));
OutputStream resultOutput = Files.newOutputStream(Path.of("rule-results.json"));
OutputStream errorOutput = Files.newOutputStream(Path.of("rule-errors.json"));
var resultWriter = mapper.writerFor(RuleResult.class).writeValuesAsArray(resultOutput);
var errorWriter = mapper.writerFor(RuleError.class).writeValuesAsArray(errorOutput)){
var validator = com.cdq.validationengine.engine.Validators.createDefault();
var summary = validator.validate(
        input,
        resultWriter::write,
        errorWriter::write);
}
```

The caller owns the input and output streams. Closing the Jackson writers completes the JSON arrays in the output files. The validator emits each result or error as soon as its rule evaluation finishes.
