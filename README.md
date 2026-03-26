# Staff Management

Staff Management is a Spring Boot + Vaadin web application for managing staff records, contracts, and absences.

## Tech Stack

- Java 8
- Spring Boot 2.7.x
- Vaadin 14
- Maven (with Maven Wrapper included)
- SQLite

## Java 8 Compatibility

This project is configured to compile with **Java 8** (`<java.version>1.8</java.version>` in `pom.xml`).

- Recommended runtime: **JDK/JRE 8**
- To verify your Java version:

```bat
java -version
```

You should see a `1.8.x` version.

## Build

You can build with Maven Wrapper (recommended, no global Maven installation required):

### Windows (CMD)

```bat
mvnw.cmd clean package
```

### Linux/macOS

```bash
./mvnw clean package
```

Build outputs:

- Executable JAR: `target/staff-management-0.0.1-SNAPSHOT.jar`
- Distribution ZIP (JAR + BAT scripts): `target/staff-management-0.0.1-SNAPSHOT-release.zip`

## Install (Windows, BAT-based distribution)

1. Build the project (`mvnw.cmd clean package`).
2. Extract `target/staff-management-0.0.1-SNAPSHOT-release.zip` into a folder of your choice.
3. Ensure these files are in the same folder:
   - `staff-management-0.0.1-SNAPSHOT.jar`
   - `start.bat`
   - `stop.bat`
4. Make sure Java 8 is installed and available (`JAVA_HOME` or `PATH`).

No additional database installation is required: SQLite DB file is created locally.

## Run with BAT scripts

From the distribution folder:

### Start

```bat
start.bat
```

`start.bat`:

- Detects Java from `JAVA_HOME`, `PATH`, or common Windows install folders.
- Prevents multiple instances using `app.pid`.
- Asks whether to reset DB:
  - `y` -> runs with `local,firstrun` profiles (schema recreation)
  - default (`N`) -> runs with `local` profile
- Starts the application in background and writes process ID into `app.pid`.

### Stop

```bat
stop.bat
```

`stop.bat` reads `app.pid`, kills the process, and removes the PID file.

## Access

Default URL:

- `http://localhost:9090`

(Port can be changed with `SERVER_PORT` environment variable.)

## Developer Run (without BAT)

For local development you can also run directly:

```bat
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

## Notes

- If `start.bat` reports no Java found, provide the JDK path when prompted.
- If the app appears already running, delete `app.pid` only after confirming no stale Java process is active.
