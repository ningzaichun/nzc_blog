package com.nzc.demo.service.impl;

import com.nzc.demo.entity.HandlerTest;
import com.nzc.demo.mapper.HandlerTestMapper;
import com.nzc.demo.service.IHandlerTestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Ning Zaichun
 * @since 2022-08-08
 */
@Service
public class HandlerTestServiceImpl extends ServiceImpl<HandlerTestMapper, HandlerTest> implements IHandlerTestService {

}
