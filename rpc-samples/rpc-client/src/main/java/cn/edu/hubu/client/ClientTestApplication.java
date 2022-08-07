package cn.edu.hubu.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author hxy
 * @Date 2022/4/6
 */

@SpringBootApplication
public class ClientTestApplication {


//    @Bean
//    public RestTemplate restTemplate() {
//        return new RestTemplate();
//    }
    public static void main(String[] args) {
        SpringApplication.run(ClientTestApplication.class,args);
    }

//    @RestController
//    public class TestController {
//
//        private final RestTemplate restTemplate;
//
//        @Autowired
//        public TestController(RestTemplate restTemplate) {this.restTemplate = restTemplate;}
//
//        @RequestMapping(value = "/echo/{str}", method = RequestMethod.GET)
//        public String echo(@PathVariable String str) {
//            return restTemplate.getForObject("http://service-provider/echo/" + str, String.class);
//        }
//    }

}
