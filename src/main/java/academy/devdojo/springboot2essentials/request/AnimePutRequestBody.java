package academy.devdojo.springboot2essentials.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnimePutRequestBody {
    private Long id;
    private String name;    
}
