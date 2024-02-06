package com.watson.order.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.watson.order.interceptor.LoginCheckInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableSwagger2
@EnableKnife4j
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginCheckInterceptor loginCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
//                "/employee/logout",  // 拦截log, 将token中的id记录到日志
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login",
                "/favicon.ico",
                "/error",    // 不排除掉 /error, 会导致后端jwt解析失败
                "/doc.html",   // 放行 api 文档页面
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };
        List<String> stringList = Arrays.stream(urls).collect(Collectors.toList());

//        registry.addInterceptor(loginCheckInterceptor).addPathPatterns("/**").excludePathPatterns("/login");
        registry.addInterceptor(loginCheckInterceptor).addPathPatterns("/**").excludePathPatterns(stringList);
    }

    /**
     * 扩展mvc框架的消息转换器
     * 可以用WebMvcConfigurer接口来配置
     *
     * @param converters 消息转换器集合
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // log.info("扩展消息转换器...");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用Jackson将Java对象转为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器对象追加到mvc框架的转换器集合中
        converters.add(0, messageConverter);
    }

    @Bean
    public Docket createRestApi() {
        // 文档类型
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.watson.order.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("校园点餐")
                .version("1.0")
                .description("校园点餐接口文档")
                .build();
    }
}
