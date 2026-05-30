package com.vrd.ecu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vrd.ecu.entity.EcuLogFile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EcuLogFileMapper extends BaseMapper<EcuLogFile> {
}
