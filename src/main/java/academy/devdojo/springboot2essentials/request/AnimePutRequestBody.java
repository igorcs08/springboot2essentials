package academy.devdojo.springboot2essentials.request;

import lombok.Data;

@Data
public class AnimePutRequestBody {
    private Long id;
    private String name;    
}
