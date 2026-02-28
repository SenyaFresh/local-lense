package ru.hse.locallense.components.entities

/**
 * Represents an item that can be interacted with (i.e., it has an action associated with it).
 * Typically used for displaying items in a list where the user can perform actions such as selecting or deleting.
 *
 * @property id The unique identifier for the item.
 * @property name The name or label that describes the item.
 */
data class ActionableItem(
    val id: Long,
    val name: String
)
