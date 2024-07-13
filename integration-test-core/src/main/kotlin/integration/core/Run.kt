package integration.core

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class Run(val onlyIf: Array<Dbms> = [], val unless: Array<Dbms> = []) {
    public companion object {
        public fun isRunnable(run: Run, dbms: Dbms): Boolean {
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
