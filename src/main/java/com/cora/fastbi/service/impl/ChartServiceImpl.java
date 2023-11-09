package com.cora.fastbi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cora.fastbi.model.entity.Chart;
import com.cora.fastbi.service.ChartService;
import com.cora.fastbi.mapper.ChartMapper;
import org.springframework.stereotype.Service;

/**
* @author Cora
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2023-11-09 17:47:19
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {

}




