# Active Context

## Current Focus
Структурная оптимизация партиклов снега. Не трогать визуальные параметры и количество.

## Recent Changes
Применены патчи (НЕ компилировались, НЕ профилировались):
- `SnowSpawnManager`: cull-объём + `shouldCull()`, спавн равномерно каждый тик (аккумулятор), инварианты вынесены из цикла, гейтинг по chunk/heightmap, радиусы <= `WeatherFogHandler.getEffectiveFogEnd()`.
- `MySnowflakeParticle`: `hasPhysics=false`, cull каждые 4 тика, проверка земли по heightmap каждые 8 тиков (`phase` 0..7).
- `WeatherFogHandler` + `FogRendererMixin`: сохраняют реальный fog end (`renderDistanceEnd`).

## Next Steps
1. Собрать (`./gradlew build`), исправить возможные имена API 26.1 (`Heightmap.Types`, `FogData.renderDistanceEnd`, `ClientLevel.getHeight/hasChunkAt`).
2. Замерить до/после: F3 `P:`, Spark, полёт на элитрах при грозе.
3. Если мало: `Mth.sin/cos`, ручное обновление x/y/z + редкий `setPos`, сужение вертикального спавна на скорости, предзаполнение при старте погоды.

## Active Decisions & Considerations
- Heightmap видит только верхнюю поверхность: снег не падает под навесы (реалистично, но отличается от текущей физики).
- Прямое изменение x/y/z без `setPos` ломает AABB для frustum culling: оставить `move()` при hasPhysics=false.
- Опционально: при высокой скорости сужать вертикальный диапазон спавна к высоте игрока (снег успевает быть перед игроком).

## Important Patterns & Preferences
Ответы короткие (caveman), решения минимальные (ponytail), точечные правки (karpathy), без заявлений «готово» без проверки.

## Learnings & Insights
Частицы спавнятся плоскостью на y+40 и падают ~0.09 бл/тик: до высоты игрока ~440 тиков, lifetime 600 -> почти все живут весь срок и копятся до лимита 32000.
