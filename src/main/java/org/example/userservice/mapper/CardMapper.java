package org.example.userservice.mapper;

import org.example.userservice.dto.CardDTO.CardRequestDTO;
import org.example.userservice.dto.CardDTO.CardResponseDTO;
import org.example.userservice.model.Card;
import org.example.userservice.utils.EncryptionUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class CardMapper {
    @Autowired
    protected EncryptionUtil encryptionService;

    @Mapping(source = "user.id", target = "userId")
    public abstract CardResponseDTO toCardResponseTo(Card card);

    public abstract Card toCard(CardRequestDTO cardRequestDTO);

    protected String map(byte[] value) {
        return encryptionService.decrypt(value);
    }

    public byte[] map(String value) {
        return encryptionService.encrypt(value);
    }
}

