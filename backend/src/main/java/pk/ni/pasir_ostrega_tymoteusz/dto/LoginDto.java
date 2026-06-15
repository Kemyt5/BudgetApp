package pk.ni.pasir_ostrega_tymoteusz.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {
    @NotBlank(message = "E-mail jest wymagany")
    @Email
    private String email;

    @NotBlank(message = "Hasło jest wymagane")
    private String password;
}
