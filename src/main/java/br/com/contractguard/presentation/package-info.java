/**
 * Presentation layer — exposes the application to the outside world via REST.
 *
 * <p><strong>Rules:</strong>
 * <ul>
 *   <li>Depends on {@code application} layer (Use Cases / Input Ports) and on {@code domain}
 *       only for Value Objects and Domain Exceptions.</li>
 *   <li>Must NOT depend on {@code infrastructure} directly.</li>
 *   <li>Contains: {@code @RestController} classes, request/response DTOs, and MapStruct mappers.</li>
 *   <li>Zero business logic in controllers — delegate everything to Use Cases.</li>
 *   <li>Always return proper HTTP status codes and use {@code @Valid} on request bodies.</li>
 * </ul>
 */
package br.com.contractguard.presentation;
