package integration.core.two

import integration.core.one.MyTestModel
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperOneToMany

@KomapperOneToMany(MyTestModel::class, "mymodel")
@KomapperEntity
public data class AnotherModel(
    @KomapperId
    val id: Long,
    val myModelId: Long,
)
