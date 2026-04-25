/**
 * Infrastructure layer — implements the Output Ports defined by the domain.
 *
 * <p><strong>Rules:</strong>
 * <ul>
 *   <li>May depend on {@code domain} and {@code application} layers.</li>
 *   <li>Must NOT be imported by {@code presentation} directly — only through interfaces.</li>
 *   <li>Contains: JPA entities ({@code persistence.entity}), Spring Data repositories
 *       ({@code persistence.repository}), port adapters ({@code persistence.adapter}),
 *       the Swagger Parser diff adapter ({@code diff}), and Spring {@code @Configuration} beans.</li>
 *   <li>JPA {@code @Entity} classes live here, NEVER in the {@code domain} layer.</li>
 * </ul>
 */
package br.com.contractguard.infrastructure;
