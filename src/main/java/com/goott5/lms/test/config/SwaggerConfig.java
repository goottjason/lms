package com.goott5.lms.test.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
    name = "sessionAuth",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.COOKIE,
    paramName = "JSESSIONID"
)
public class SwaggerConfig {

  public OpenAPI custumOpenAPI() {

    return new OpenAPI().info(
        new Info().title("LMS Project API 문서").description("Test API 문서").version("1.0.0")
    );
  }

}
