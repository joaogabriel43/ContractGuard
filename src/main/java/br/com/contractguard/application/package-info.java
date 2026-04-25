/**
 * Application layer — orchestrates domain objects to fulfill use cases.
 *
 * <p><strong>Rules:</strong>
 * <ul>
 *   <li>Depends only on the {@code domain} layer.</li>
 *   <li>Contains Use Cases (interactors), Commands, Queries, and application-level DTOs.</li>
 *   <li>Must NOT depend on Spring annotations (except {@code @Service} / {@code @Component}
 *       for wiring — no {@code @RestController}, {@code @Entity}, etc.).</li>
 *   <li>Use Cases implement Input Ports ({@code domain.port.in}) and call Output Ports
 *       ({@code domain.port.out}) via interface — never concrete infrastructure classes.</li>
 * </ul>
 */
package br.com.contractguard.application;
