package org.example.userservice.mapper;

import org.example.userservice.dto.CardTO.CardRequestTO;
import org.example.userservice.dto.CardTO.CardResponseTO;
import org.example.userservice.model.Card;
import org.example.userservice.utils.EncryptionUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class  CardMapper  {
    @Autowired
    protected EncryptionUtil encryptionService;

    @Mapping(source = "user.id", target = "userId")
    public abstract CardResponseTO toCardResponseTo(Card card);

    public abstract Card toCard(CardRequestTO cardRequestTo);

    protected String map(byte[] value) {
        return encryptionService.decrypt(value);
    }

    public byte[] map(String value) {
        return encryptionService.encrypt(value);
    }
}

