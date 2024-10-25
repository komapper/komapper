package org.komapper.core.dsl.metamodel

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.TableExpression
import java.time.Clock
import kotlin.reflect.KClass

/**
 * This interface represents the metamodel of an entity.
 * It provides methods to access and manipulate the properties of the entity.
 *
 * @param ENTITY The type of the entity.
 * @param ID The type of the entity's identifier.
 * @param META The type of the metamodel.
 */
@ThreadSafe
interface EntityMetamodel<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> : TableExpression<ENTITY> {
    companion object {
        /**
         * The version of the metamodel.
         * This version will be incremented as the specification changes.
         */
        const val METAMODEL_VERSION: Int = 2
    }

    /**
     * Returns the declaration of the metamodel.
     *
     * @return The declaration of the metamodel.
     */
    fun declaration(): EntityMetamodelDeclaration<META>

    /**
     * Returns the ID generator of the entity.
     * The ID generator is responsible for generating unique identifiers for each entity instance.
     * This method may return null if the entity does not have an ID generator.
     *
     * @return The ID generator of the entity, or null if none exists.
     */
    fun idGenerator(): IdGenerator<ENTITY, ID>?

    /**
     * Returns the ID properties of the entity.
     * The ID properties are the properties that uniquely identify an entity.
     *
     * @return A list of PropertyMetamodel objects representing the ID properties of the entity.
     */
    fun idProperties(): List<PropertyMetamodel<ENTITY, *, *>>

    /**
     * Returns the virtual ID properties of the entity.
     * Virtual ID properties are additional properties that can be used to identify an entity.
     * By default, this method returns an empty list.
     *
     * @return A list of PropertyMetamodel objects representing the virtual ID properties of the entity.
     */
    fun virtualIdProperties(): List<PropertyMetamodel<ENTITY, *, *>> = emptyList()

    /**
     * Returns the version property of the entity.
     * The version property is used to implement optimistic locking.
     *
     * @return The PropertyMetamodel object representing the version property of the entity, or null if none exists.
     */
    fun versionProperty(): PropertyMetamodel<ENTITY, *, *>?

    /**
     * Returns the created-at property of the entity.
     * The created-at property is used to store the timestamp when the entity was created.
     *
     * @return The PropertyMetamodel object representing the created-at property of the entity, or null if none exists.
     */
    fun createdAtProperty(): PropertyMetamodel<ENTITY, *, *>?

    /**
     * Returns the updated-at property of the entity.
     * The updated-at property is used to store the timestamp when the entity was last updated.
     *
     * @return The PropertyMetamodel object representing the updated-at property of the entity, or null if none exists.
     */
    fun updatedAtProperty(): PropertyMetamodel<ENTITY, *, *>?

    /**
     * Returns the properties of the entity.
     * The properties are the fields or columns of the entity.
     *
     * @return A list of PropertyMetamodel objects representing the properties of the entity.
     */
    fun properties(): List<PropertyMetamodel<ENTITY, *, *>>

    /**
     * Extracts the ID from the given entity.
     * This method retrieves the identifier of the entity.
     *
     * @param e The entity from which to extract the ID.
     * @return The ID of the entity.
     */
    fun extractId(e: ENTITY): ID

    /**
     * Converts the generated key to the entity's ID.
     * This method is used to convert a generated key (e.g., from a database auto-incremented value) to the entity's identifier type.
     *
     * @param generatedKey The generated key to convert.
     * @return The converted ID, or null if the conversion is not possible.
     */
    fun convertToId(generatedKey: Long): ID?

    /**
     * Returns the version assignment for the entity.
     * The version assignment is used to implement optimistic locking by assigning a version value to the entity.
     *
     * @return A pair containing the version property and the operand representing the version value, or null if no version property exists.
     */
    fun versionAssignment(): Pair<PropertyMetamodel<ENTITY, *, *>, Operand>?

    /**
     * Returns the created-at assignment for the entity.
     * The created-at assignment is used to set the timestamp when the entity was created.
     *
     * @param c The clock used to generate the timestamp.
     * @return A pair containing the created-at property and the operand representing the timestamp, or null if no created-at property exists.
     */
    fun createdAtAssignment(c: Clock): Pair<PropertyMetamodel<ENTITY, *, *>, Operand>?

    /**
     * Returns the updated-at assignment for the entity.
     * The updated-at assignment is used to set the timestamp when the entity was last updated.
     *
     * @param c The clock used to generate the timestamp.
     * @return A pair containing the updated-at property and the operand representing the timestamp, or null if no updated-at property exists.
     */
    fun updatedAtAssignment(c: Clock): Pair<PropertyMetamodel<ENTITY, *, *>, Operand>?

    /**
     * Pre-insert hook for the entity.
     * This method is called before the entity is inserted into the database.
     *
     * @param e The entity to be inserted.
     * @param c The clock used to generate the timestamp.
     * @return The entity to be inserted, possibly modified.
     */
    fun preInsert(e: ENTITY, c: Clock): ENTITY

    /**
     * Pre-update hook for the entity.
     * This method is called before the entity is updated in the database.
     *
     * @param e The entity to be updated.
     * @param c The clock used to generate the timestamp.
     * @return The entity to be updated, possibly modified.
     */
    fun preUpdate(e: ENTITY, c: Clock): ENTITY

    /**
     * Post-update hook for the entity.
     * This method is called after the entity is updated in the database.
     *
     * @param e The entity that was updated.
     * @return The updated entity, possibly modified.
     */
    fun postUpdate(e: ENTITY): ENTITY

    /**
     * Retrieves a property metamodel by its name.
     * This method searches for a property metamodel with the specified name among the entity's properties.
     *
     * @param name The name of the property to retrieve.
     * @return The PropertyMetamodel object representing the property, or null if no property with the specified name exists.
     */
    operator fun get(name: String): PropertyMetamodel<ENTITY, *, *>? {
        return properties().find { it.name == name }
    }

    /**
     * Creates a new entity instance from the given property values.
     * This method constructs a new entity using the provided map of property metamodels and their corresponding values.
     *
     * @param m A map where the keys are PropertyMetamodel objects and the values are the property values.
     * @return A new instance of the entity.
     */
    fun newEntity(m: Map<PropertyMetamodel<*, *, *>, Any?>): ENTITY

    /**
     * Creates a new metamodel instance with the specified parameters.
     * This method constructs a new metamodel using the provided table, catalog, schema, and other settings.
     *
     * @param table The name of the table.
     * @param catalog The name of the catalog.
     * @param schema The name of the schema.
     * @param alwaysQuote Whether to always quote identifiers.
     * @param disableSequenceAssignment Whether to disable sequence assignment.
     * @param declaration The declaration of the metamodel.
     * @param disableAutoIncrement Whether to disable auto-increment (default is false).
     * @return A new instance of the metamodel.
     */
    fun newMetamodel(
        table: String,
        catalog: String,
        schema: String,
        alwaysQuote: Boolean,
        disableSequenceAssignment: Boolean,
        declaration: EntityMetamodelDeclaration<META>,
        disableAutoIncrement: Boolean = false,
    ): META

    /**
     * Returns a string representation of the entity.
     * If the property is set to be masked, its data will be displayed as `*****`.
     *
     * @param entity The entity to convert to a string.
     * @return A string representation of the entity.
     */
    fun toText(entity: ENTITY): String {
        val props = properties().joinToString(", ") { "${it.name}=${if (it.masking) "*****" else it.getter(entity)}" }
        return "${klass().simpleName}($props)"
    }
}

/**
 * Abstract class representing a stub implementation of the EntityMetamodel interface.
 * This class provides default implementations for the methods in the EntityMetamodel interface,
 * which throw an error indicating that the method needs to be implemented.
 *
 * @param ENTITY The type of the entity.
 * @param META The type of the metamodel.
 */
@Suppress("unused")
abstract class EntityMetamodelStub<ENTITY : Any, META : EntityMetamodelStub<ENTITY, META>> :
    EntityMetamodel<ENTITY, Any, META> {
    override fun klass(): KClass<ENTITY> = fail()
    override fun tableName(): String = fail()
    override fun catalogName(): String = fail()
    override fun schemaName(): String = fail()
    override fun alwaysQuote(): Boolean = fail()
    override fun disableSequenceAssignment(): Boolean = fail()
    override fun declaration(): EntityMetamodelDeclaration<META> = fail()
    override fun idGenerator(): IdGenerator<ENTITY, Any>? = fail()
    override fun idProperties(): List<PropertyMetamodel<ENTITY, *, *>> = fail()
    override fun versionProperty(): PropertyMetamodel<ENTITY, *, *>? = fail()
    override fun createdAtProperty(): PropertyMetamodel<ENTITY, *, *>? = fail()
    override fun updatedAtProperty(): PropertyMetamodel<ENTITY, *, *>? = fail()
    override fun properties(): List<PropertyMetamodel<ENTITY, *, *>> = fail()
    override fun newEntity(m: Map<PropertyMetamodel<*, *, *>, Any?>): ENTITY = fail()
    override fun extractId(e: ENTITY): Any = fail()
    override fun convertToId(generatedKey: Long): Any = fail()
    override fun versionAssignment(): Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? = fail()
    override fun createdAtAssignment(c: Clock): Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? = fail()
    override fun updatedAtAssignment(c: Clock): Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? = fail()
    override fun preInsert(e: ENTITY, c: Clock): ENTITY = fail()
    override fun preUpdate(e: ENTITY, c: Clock): ENTITY = fail()
    override fun postUpdate(e: ENTITY): ENTITY = fail()

    override fun newMetamodel(
        table: String,
        catalog: String,
        schema: String,
        alwaysQuote: Boolean,
        disableSequenceAssignment: Boolean,
        declaration: EntityMetamodelDeclaration<META>,
        disableAutoIncrement: Boolean,
    ): META = fail()

    private fun fail(): Nothing {
        error("Fix google/ksp compile errors.")
    }
}

/**
 * An internal object representing an empty metamodel.
 * This object provides default implementations for the methods in the EntityMetamodel interface,
 * which throw an UnsupportedOperationException.
 */
internal object EmptyMetamodel : EntityMetamodel<Nothing, Nothing, EmptyMetamodel> {
    override fun declaration(): EntityMetamodelDeclaration<EmptyMetamodel> {
        return {}
    }

    override fun idGenerator(): IdGenerator<Nothing, Nothing>? {
        return null
    }

    override fun idProperties(): List<PropertyMetamodel<Nothing, *, *>> {
        return emptyList()
    }

    override fun versionProperty(): PropertyMetamodel<Nothing, *, *>? {
        return null
    }

    override fun createdAtProperty(): PropertyMetamodel<Nothing, *, *>? {
        return null
    }

    override fun updatedAtProperty(): PropertyMetamodel<Nothing, *, *>? {
        return null
    }

    override fun properties(): List<PropertyMetamodel<Nothing, *, *>> {
        return emptyList()
    }

    override fun convertToId(generatedKey: Long): Nothing? {
        return null
    }

    override fun versionAssignment(): Pair<PropertyMetamodel<Nothing, *, *>, Operand>? {
        return null
    }

    override fun createdAtAssignment(c: Clock): Pair<PropertyMetamodel<Nothing, *, *>, Operand>? {
        return null
    }

    override fun updatedAtAssignment(c: Clock): Pair<PropertyMetamodel<Nothing, *, *>, Operand>? {
        return null
    }

    override fun newEntity(m: Map<PropertyMetamodel<*, *, *>, Any?>): Nothing {
        throw UnsupportedOperationException()
    }

    override fun newMetamodel(
        table: String,
        catalog: String,
        schema: String,
        alwaysQuote: Boolean,
        disableSequenceAssignment: Boolean,
        declaration: EntityMetamodelDeclaration<EmptyMetamodel>,
        disableAutoIncrement: Boolean,
    ): EmptyMetamodel {
        throw UnsupportedOperationException()
    }

    override fun postUpdate(e: Nothing): Nothing {
        throw UnsupportedOperationException()
    }

    override fun preUpdate(e: Nothing, c: Clock): Nothing {
        throw UnsupportedOperationException()
    }

    override fun preInsert(e: Nothing, c: Clock): Nothing {
        throw UnsupportedOperationException()
    }

    override fun extractId(e: Nothing): Nothing {
        throw UnsupportedOperationException()
    }

    override fun klass(): KClass<Nothing> {
        throw UnsupportedOperationException()
    }

    override fun tableName(): String {
        throw UnsupportedOperationException()
    }

    override fun catalogName(): String {
        throw UnsupportedOperationException()
    }

    override fun schemaName(): String {
        throw UnsupportedOperationException()
    }

    override fun alwaysQuote(): Boolean {
        return false
    }

    override fun disableSequenceAssignment(): Boolean {
        return false
    }
}
