package example.spring.boot.jdbc

import com.google.cloud.sqlcommenter.interceptors.SpringSQLCommenterInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@EnableWebMvc
@Configuration
class WebConfig : WebMvcConfigurer {

    @Bean
    fun sqlInterceptor(): SpringSQLCommenterInterceptor {
        return SpringSQLCommenterInterceptor()
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(sqlInterceptor())
    }
}
