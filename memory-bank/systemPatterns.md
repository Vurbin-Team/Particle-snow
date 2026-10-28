# System Patterns

## Architecture Overview
Только клиент. Статические синглтоны, без DI.

## Key Technical Decisions
- Партикл `MySnowflakeParticle extends SingleQuadParticle`, слой TRANSLUCENT, свет всегда максимальный (15728880).
- Два типа (`my_snowflake`, `my_snowflake_realistic`) делят один класс; отличаются только спрайтами. Выбор: `ParticleTypes.getActiveSnowType()`.
- Спавн: `SnowSpawnManager` на `ClientTickEvents.END_LEVEL_TICK`. Центр диска смещён на `playerVelocity*5` вперёд и на `-windDrift` назад.
- Статический конфиг `SnowfallConfig` (поля public static), JSON через `SnowfallConfigManager`, `save()` вызывает `SnowSpawnManager.reload()`.
- `IncreaseParticleLimitMixin`: `@ModifyConstant(16384)` в `ParticleGroup.<init>` -> `MAX_PARTICLE_COUNT` (32000). Читается один раз при создании группы.

## Design Patterns in Use
Mixin-инъекции с `ci.cancel()` для отключения ванильной погоды. Static utility managers.

## Component Relationships
`CinematicSnowstormClient` -> `ModSounds`, `AdvancedWeatherSoundManager`, `SnowfallConfigManager.load()`, `ParticleTypes.register()`, провайдеры, `SnowSpawnManager.init()`.
`MySnowflakeParticle.tick()` читает `SnowSpawnManager.getPlayerVelocity()` и `SnowfallConfig`.
`FogRendererMixin` -> `WeatherFogHandler` -> `FogConfig`.

## Critical Implementation Paths
Спавн: `SnowSpawnManager` цикл -> `world.addParticle(type, true, true, ...)` -> Factory -> конструктор (≈8 вызовов random, trig для ветра).
Тик партикла: старение -> fade -> sway (sin/cos) -> `hasChunkAt` -> `move()` с коллизией -> удаление при onGround/блокировке.
