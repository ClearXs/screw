package com.jw.screw.admin.sys.datasource.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jw.screw.admin.common.EntityFactoryBuilder;
import com.jw.screw.admin.common.constant.DataOperationState;
import com.jw.screw.admin.common.constant.DataTypeEnum;
import com.jw.screw.admin.common.exception.BasicOperationException;
import com.jw.screw.admin.common.exception.UnknowOperationException;
import com.jw.screw.admin.common.util.SqlUtils;
import com.jw.screw.admin.common.validate.Validators;
import com.jw.screw.admin.sys.datasource.dao.DatasourceDao;
import com.jw.screw.admin.sys.datasource.dto.DatasourceAddDTO;
import com.jw.screw.admin.sys.datasource.dto.DatasourceUpdateDTO;
import com.jw.screw.admin.sys.datasource.entity.Datasource;
import com.jw.screw.admin.sys.datasource.model.DatasourceVO;
import com.jw.screw.admin.sys.datasource.service.DatasourceService;
import com.jw.screw.admin.sys.server.dao.AppServerDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DatasourceServiceImpl implements DatasourceService {

    @Resource
    private DatasourceDao datasourceDao;

    @Resource
    private AppServerDao appServerDao;

    @Override
    public Integer addDatasource(DatasourceAddDTO datasourceAddDTO) throws InstantiationException, IllegalAccessException, BasicOperationException {
        Validators.doExist(datasourceDao, datasourceAddDTO);
        Datasource datasource = new EntityFactoryBuilder<Datasource>()
                .setVo(datasourceAddDTO)
                .setEntityClass(Datasource.class)
                .build();
        Validators.doResult(datasourceDao.insert(datasource));
        return DataOperationState.SUCCESSFUL;
    }

    @Override
    public Integer updateDatasource(DatasourceUpdateDTO datasourceUpdateDTO) throws InstantiationException, IllegalAccessException, BasicOperationException {
        Validators.doExist(datasourceDao, datasourceUpdateDTO);
        Datasource datasource = new EntityFactoryBuilder<Datasource>()
                .setVo(datasourceUpdateDTO)
                .setEntityClass(Datasource.class)
                .build();
        Validators.doResult(datasourceDao.updateById(datasource));
        return DataOperationState.SUCCESSFUL;

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer deleteDatasource(String id) throws BasicOperationException {

        Validators.doResult(datasourceDao.deleteById(id));
        // 更新与此数据库关联的服务
        Validators.doResult(appServerDao.deleteDataSourceUpdateServer(id));
        return DataOperationState.SUCCESSFUL;
    }

    @Override
    public List<DatasourceVO> queryAllDatasource() throws InstantiationException, IllegalAccessException {
        List<Datasource> datasources = datasourceDao.selectList(new QueryWrapper<>());
        List<DatasourceVO> datasourceVOList = new ArrayList<>();
        for (Datasource datasource : datasources) {
            DatasourceVO datasourceVO = new EntityFactoryBuilder<DatasourceVO>()
                    .setEntityClass(DatasourceVO.class)
                    .setVo(datasource)
                    .build();
            datasourceVOList.add(datasourceVO);
        }
        return datasourceVOList;
    }

    @Override
    public boolean testConnection(String dataSourceId) {
        try {
            Datasource datasource = datasourceDao.selectOne(new QueryWrapper<Datasource>().eq("ID", dataSourceId));
            if (ObjectUtils.isEmpty(datasource)) {
                throw new UnknowOperationException("数据库数据获取失败，无法进行测试");
            }
            DataTypeEnum dataTypeEnum = DataTypeEnum.typeOf(datasource.getDatasourceType());
            if (BeanUtil.isEmpty(dataTypeEnum)) {
                throw new NullPointerException("无法识别的数据库类型: " + datasource.getDatasourceType() + "");
            }
            String jdbcUrl = dataTypeEnum.getJdbcUrl()
                    .replace("{ip}", datasource.getDatasourceIp())
                    .replace("{port}", datasource.getDatasourcePort())
                    .replace("{dbname}", datasource.getDatasourceConnectName());
            return SqlUtils.testConnection(jdbcUrl, datasource.getDatasourceUsername(), datasource.getDatasourcePassword());
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }
}
