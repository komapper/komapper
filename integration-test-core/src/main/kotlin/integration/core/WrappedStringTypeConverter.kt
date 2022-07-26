package integration.core

import org.komapper.core.spi.DataTypeConverter
import kotlin.reflect.KClass

class WrappedStringTypeConverter : DataTypeConverter<WrappedString, String> {
    override val exteriorClass: KClass<WrappedString> = WrappedString::class
    override val interiorClass: KClass<String> = String::class

    override fun unwrap(exterior: WrappedString): String {
        return exterior.value
    }

    override fun wrap(interior: String): WrappedString {
        return WrappedString(interior)
    }
}
