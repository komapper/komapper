package integration.core

import org.komapper.core.spi.DataTypeConverter
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class WrappedStringTypeConverter : DataTypeConverter<WrappedString, String> {
    override val exteriorType: KType = typeOf<WrappedString>()
    override val interiorType: KType = typeOf<String>()

    override fun unwrap(exterior: WrappedString): String {
        return exterior.value
    }

    override fun wrap(interior: String): WrappedString {
        return WrappedString(interior)
    }
}
