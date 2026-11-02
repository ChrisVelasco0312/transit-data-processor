# `registros.json` — Data and Edge-Case Guide

Mock dataset for the `Registro` model in `edu.iudigital.transit`.

- **File:** `src/main/resources/registros.json`
- **Records:** 100
- **Encoding:** UTF-8 (no BOM)
- **Root:** JSON array of `Registro` objects
- **Order:** chronological except for one deliberate out-of-order pair (records 55–56)

## Record contract

Each object has exactly these five fields:

| Field | Type | Notes |
|---|---|---|
| `idUsuario` | string or `null` | User identifier |
| `ruta` | string or `null` | Route identifier |
| `estacion` | string or `null` | Station name |
| `accion` | string | Always `ENTRADA` or `SALIDA` |
| `fecha` | string | ISO-8601 local date-time, `yyyy-MM-ddTHH:mm:ss` |

`fecha` matches `LocalDateTime.parse(...)` and has no timezone or offset.

## Dataset summary

| Metric | Value |
|---|---|
| Total records | 100 |
| Dates | 2026-03-15 (4), 2026-03-16 (92), 2026-03-17 (4) |
| Actions | 53 `ENTRADA`, 47 `SALIDA` |
| Distinct user values | 20 standard IDs, 4 variants, 1 empty, 1 `null` |
| Routes | `R-01` to `R-08`, `r-02` variant, `null` |
| Stations | 12 canonical names, 2 spelling variants, empty, `null` |

Route distribution:

| Route | Records |
|---|---:|
| `R-01` | 19 |
| `R-02` | 15 |
| `R-03` | 14 |
| `R-05` | 13 |
| `R-06` | 11 |
| `R-04` | 10 |
| `R-07` | 8 |
| `R-08` | 6 |
| `r-02` | 2 |
| `null` | 2 |

Station distribution (canonical names only):

| Station | Records |
|---|---:|
| Centro | 40 |
| Portal Norte | 17 |
| Terminal Sur | 8 |
| Universidad | 7 |
| Hospital | 6 |
| Estación Central | 5 |
| Estadio | 5 |
| Aeropuerto | 3 |
| Av. 5 de Mayo | 2 |
| La Riviera | 2 |
| Plaza de Mercado | 1 |
| Los Álamos | 0 |

## Edge cases by category

Record numbers below refer to the 1-based position in the JSON array.

### 1. Midnight and date boundaries

| Case | Records | Description |
|---|---|---|
| Cross-midnight trip, Sunday → Monday | 3 → 8 | `U003` enters Portal Norte at `23:52` and exits at Centro at `00:07`. |
| Cross-midnight trip, Monday → Tuesday | 95 → 97 | `U004` enters Centro at `23:50` and exits at Terminal Sur at `00:04`. |
| Full 24-hour coverage on the main day | 5–96 | Monday includes records from `00:00:00` to `23:55:00`. |
| Early-morning records | 12–16 | Activity between `04:58` and `05:20`. |
| Late-night records | 93–96 | Activity between `19:10` and `23:55`, plus records 97–100 after midnight. |
| Multi-day dataset | 1–100 | Three consecutive dates allow grouping tests by `LocalDate`. |

### 2. Same timestamp and zero-duration trips

| Case | Records | Description |
|---|---|---|
| Three records at the same instant | 5, 6, 7 | All at `2026-03-16T00:00:00`. |
| Zero-duration, zero-distance trip | 5 → 6 | `U005` has `ENTRADA` and `SALIDA` at the same station and timestamp. |
| Simultaneous different users | 5–7 | `U005` and `U009` share the same timestamp. |
| Duplicate exact record | 98, 99 | Two objects have identical field values. |

### 3. Action-sequence anomalies

| Case | Records | Description |
|---|---|---|
| Orphan `SALIDA` without a preceding `ENTRADA` | 4 | `U002` exits Centro on Sunday with no prior entry. |
| Orphan `SALIDA` without a preceding `ENTRADA` | 61 | `U015` exits La Riviera without a prior entry. |
| Orphan `SALIDA` without a preceding `ENTRADA` | 65 | `U018` exits La Riviera without a prior entry. |
| Orphan `SALIDA` without a preceding `ENTRADA` | 78 | Lowercase user `u001` exits Centro without a prior entry. |
| Double `ENTRADA` | 14, 15 | `U012` validates entry twice before exiting at record 16. |
| Open trip with no exit | 52 | `U012` enters Estadio at `08:10` and has no matching exit. |
| Open trip with no exit | 68 | `U020` enters Estadio at `12:45`; the next exit by this user is several hours later at another station. |
| Open trip with no exit | 93 | `U006` enters Estadio at `19:10` and never exits. |
| Route change | 38 → 43, then 56 → 55 (chronological order) | `U004` uses `R-04`, exits, later enters `R-05` at Hospital and exits at Centro. |

### 4. Travel-time extremes

| Case | Records | Description |
|---|---|---|
| Less than one minute | 85 → 86 | `U013` moves from Universidad to Centro in 30 seconds. |
| Short trip, 1–5 minutes | 17 → 19 | `U013` takes 3 minutes. |
| Normal trip, around 10–20 minutes | 1 → 2, 48 → 54 | 14 and 17 minutes respectively. |
| Medium trip, 50–70 minutes | 69 → 71 (125 min) and 70 → 72 (70 min) | Long but plausible journeys. |
| Over two hours | 69 → 71 | `U009` takes 2 hours 5 minutes from Centro to Terminal Sur. |
| Negative duration if file order is trusted | 55 → 56 | File shows `09:15` before `09:05`; sorting by `fecha` is required. |

### 5. Ordering

| Case | Records | Description |
|---|---|---|
| Deliberate out-of-order pair | 55, 56 | Record 55 has a later timestamp than record 56. |
| Otherwise chronological | 1–100 | All other records are sorted ascending by `fecha`. |

### 6. Dirty identifiers and strings

| Case | Record | Value |
|---|---|---|
| Lowercase user ID | 78 | `u001` (does not match `U001` under case-sensitive comparison) |
| User ID with surrounding spaces | 84 | `" U012 "` |
| Long/irregular user ID | 73, 74 | `USR-2026-0008` |
| Empty user ID | 94 | `""` |
| `null` user ID | 96 | `null` |
| Lowercase route code | 87, 88 | `r-02` (does not match `R-02` under case-sensitive comparison) |
| Accent inconsistency | 21 vs 87 | `Estación Central` vs `Estacion Central` |
| Station with trailing space | 91 | `"Portal Norte "` |
| Empty station | 96 | `""` |
| `null` station | 100 | `null` |
| `null` route | 98, 99 | `null` |

### 7. Aggregation edge cases

| Case | Records | Description |
|---|---|---|
| High-volume route | `R-01` | 19 records. |
| Low-volume route | `R-08` | 6 records. |
| Station with entries only | `Aeropuerto` | Records 10, 44, 59; all are `ENTRADA`. |
| Station with exits only | `La Riviera` | Records 61, 65; both are `SALIDA`. |
| Station with zero records | `Los Álamos` | Intentionally absent from the file; absence represents zero activity. |
| Peak hour, morning | 17–54 | 38 records between `06:00` and `08:59`. |
| Peak hour, evening | 75–92 | 18 records between `17:00` and `18:59`. |
| Valley hours | 55–74 | 20 records between `09:00` and `15:55`. |
| User with balanced activity | `U001` | 5 records across both actions and multiple hours. |
| User appearing on multiple days | `U001`, `U003`, `U004` | Records span March 15–17. |
| User with a single record | `u001`, `" U012 "`, `""`, `null` | One record each. |
| Duplicate counting | 98, 99 | Exact duplicates should not be silently double-counted if deduplication is required. |

### 8. Null-handling cases

| Field | Records | Description |
|---|---|---|
| `idUsuario` | 96 | One `null` value. |
| `ruta` | 98, 99 | Two `null` values. |
| `estacion` | 100 | One `null` value. |
| `accion` | — | Never `null`; values are limited to `ENTRADA` and `SALIDA`. |
| `fecha` | — | Never `null`; every value is a valid ISO local date-time. |

## Known data-quality decisions

- Action values are uppercase to match the `Accion` enum (`ENTRADA`, `SALIDA`).
- Dates use `LocalDateTime`-compatible ISO strings without timezone.
- Invalid enum values and malformed timestamps are **not** included, so the file remains parseable as a whole.
- `null` and empty-string values are intentionally present to test null-safe pipelines.
- The file lives in Maven's standard `src/main/resources/` directory, so it is copied to `target/classes/` and included in the packaged JAR.

## Quick validation

```bash
jq 'length' src/main/resources/registros.json
jq '[.[].accion] | group_by(.) | map({accion: .[0], total: length})' src/main/resources/registros.json
jq '[.[].fecha[0:10]] | group_by(.) | map({fecha: .[0], total: length})' src/main/resources/registros.json
```
