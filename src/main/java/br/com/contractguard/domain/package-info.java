/**
 * Domain layer — the innermost ring of the Clean Architecture.
 *
 * <p><strong>Rules:</strong>
 *
 * <ul>
 *   <li>Zero dependencies on Spring, JPA, or any external framework.
 *   <li>Contains Aggregates, Entities, Value Objects, Domain Services, and Port interfaces.
 *   <li>All classes must be pure Java (records preferred for immutability).
 *   <li>Lombok is allowed only for convenience; never {@code @Entity} or {@code @Table}.
 * </ul>
 */
package br.com.contractguard.domain;
