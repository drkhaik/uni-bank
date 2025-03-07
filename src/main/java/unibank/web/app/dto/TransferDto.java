package unibank.web.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import  lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class TransferDto {

    private long recipientAccountNumber;
    private double amount;
    private String code;
}
