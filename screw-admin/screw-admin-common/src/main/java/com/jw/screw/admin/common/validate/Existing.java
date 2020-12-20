package com.jw.screw.admin.common.validate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
class Existing {

    private ExistMapper existMapper;

    private Object entity;

}