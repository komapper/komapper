package org.komapper.core

sealed interface Command<T>
interface First<T : Any> : Command<T>
interface FirstOrNull<T> : Command<T>
interface Single<T : Any> : Command<T>
interface Multiple<T : Any> : Command<List<T>>
interface Execute : Command<Long>
