# Tech Context

## Technologies Used
Java 25, Fabric Loom 1.18-SNAPSHOT, Minecraft 26.1 (Mojang names), Fabric Loader 0.19.5, Fabric API 0.145.1+26.1, Mixin (JAVA_17 compat level).

## Development Setup
`./gradlew runClient`, `./gradlew build`. Gradle jvmargs -Xmx4G. CI: `.github/workflows/build.yml`.

## Technical Constraints
- API 26.1 свежее знаний модели: имена классов/методов проверять компиляцией, не по памяти.
- Mixin `defaultRequire: 1`: при несовпадении сигнатуры игра не стартует.

## Dependencies
Mod Menu 18.0.2 и Cloth Config 26.1.154 (опционально, suggests).

## Tool Usage Patterns
Профилирование: F3 (счётчик `P:`), Spark. Сборка и замеры делаются пользователем локально (в песочнице Claude нет Maven Mojang/Fabric).
