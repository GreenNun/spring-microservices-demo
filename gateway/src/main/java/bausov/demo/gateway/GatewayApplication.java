package bausov.demo.gateway;

//import io.ap4k.kubernetes.annotation.KubernetesApplication;

import io.dekorate.kubernetes.annotation.ImagePullPolicy;
import io.dekorate.kubernetes.annotation.KubernetesApplication;
import io.dekorate.kubernetes.annotation.Probe;
import io.dekorate.kubernetes.annotation.ServiceType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
@KubernetesApplication(
        group = "greennun",
        livenessProbe = @Probe(httpActionPath = "/actuator/health"),
        readinessProbe = @Probe(httpActionPath = "/actuator/health"),
        serviceType = ServiceType.NodePort,
        imagePullPolicy = ImagePullPolicy.Always
)
@RestController
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p -> p.path("/test**").filters(f ->
                        f.hystrix(c -> c.setName("test").setFallbackUri("forward:/fallback"))).uri("lb://api-test1"))
                .route(p -> p.path("/test1/**").filters(f ->
                        f.hystrix(c -> c.setName("test1").setFallbackUri("forward:/fallback"))).uri("lb://api-test1"))
                .route(p -> p.path("/test2/**").filters(f ->
                        f.hystrix(c -> c.setName("test2").setFallbackUri("forward:/fallback"))).uri("lb://api-test2"))
                .build();
    }

    @GetMapping("/fallback")
    public ResponseEntity<List<Object>> fallback() {
        System.out.println("fallback enabled");
        HttpHeaders headers = new HttpHeaders();
        headers.add("fallback", "true");
        return ResponseEntity.ok().headers(headers).body(Collections.emptyList());
    }
}
