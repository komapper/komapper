package integration.setting

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Run(val onlyIf: Array<Dbms> = [], val unless: Array<Dbms> = []) {
    companion object {
        fun isRunnable(run: Run, dbms: Dbms): Boolean {
            with(run) {
                if (onlyIf.isNotEmpty()) {
                    return dbms in onlyIf
                }
                if (unless.isNotEmpty()) {
                    return dbms !in unless
                }
            }
            return true
        }
    }
}
