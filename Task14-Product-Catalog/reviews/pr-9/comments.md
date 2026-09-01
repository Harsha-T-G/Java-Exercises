# PR #9 review comments — fix order

| # | Path | Line | Status | Action |
|---|------|------|--------|--------|
| 1 | `service/ProductService.java` | create | Fixed | Reverted `SERIALIZABLE` (caused concurrent-create 500); documented max-product race in SELF_REVIEW |
| 2 | `service/ProductService.java` | update/adjustStock | Fixed | All writes use `saveAndFlush` before mapping response |
| 3 | `dto/ProductRequest.java` | price | Fixed | `@Digits(17,2)` aligns API with `NUMERIC(19,2)`; README updated |
| 4 | `exception/GlobalExceptionHandler.java` | — | Fixed | `HttpMessageNotReadableException` → 400; safe 500 logging (type only, no throwable message) |
| 5 | `service/ProductPageRequestFactory.java` | createPageable | Fixed | Append `id ASC` tie-breaker; `ProductPageRequestFactoryTest` added |
| 6 | `resources/application.yml` | management | Fixed | `show-details: always` in base config for `components.db` |
| 7 | `service/ProductPersistenceSupport.java` | — | Fixed | Constraint-name chain walk; no message fallback; stock writes routed here |
| 8 | `ProductIntegrationTest.java` | resetCatalog | Fixed | `productRepository.deleteAll()` |
| 9 | `compose.yml` / `.env.example` | ports/password | Fixed | Bind `127.0.0.1:5432`; remove `changeme` default |
| 10 | Tests (multiple) | — | Fixed | Persistence support integration, page factory, handler, optimistic-lock unit tests, exception messages |

## Remaining open (dismiss or defer)

- `/api/v1` versioning — exercise API contract
- Spring Security / rate limiting — not in Week 6 brief
- `If-Match` optimistic-lock preconditions — enhancement
- Five-arg pagination signature → criteria object — refactor
- Shared test fixture extraction — refactor
- JDBC timeout configuration — follow-up
- Low-stock unbounded list — acceptable at exercise scale

## Sample comment bodies

### 1. SERIALIZABLE regression
> Concurrent creates with different SKUs can fail with HTTP 500 under `SERIALIZABLE`.

**Fix:** Revert to default isolation; max-product guard remains best-effort at configured scale.

### 2. Flush before response
> PUT/PATCH responses can return stale `updatedAt`/`version`.

**Fix:** `saveAndFlush` before `toResponse`.

### 3. Price precision
> API accepts values PostgreSQL rounds or rejects.

**Fix:** `@Digits(integer=17, fraction=2)`.

### 4. Malformed JSON → 500
**Fix:** Dedicated `HttpMessageNotReadableException` handler.

### 5. Pagination tie-breaker
**Fix:** `parseSort(sort).and(Sort.by(ASC, "id"))` + unit test.

### 6. Health db component hidden
**Fix:** Move `show-details: always` to base `application.yml`.

### 7. SKU constraint mapping
**Fix:** `ProductPersistenceSupport` inspects `ConstraintViolationException` chain only.

### 8. Integration test cleanup
**Fix:** `deleteAll()` instead of first-page delete loop.

### 9. Compose exposure
**Fix:** Localhost bind + placeholder passwords in `.env.example`.

### 10. Test gaps
**Fix:** Added `ProductPersistenceSupportIntegrationTest`, `ProductPageRequestFactoryTest`, expanded handler/service tests.
