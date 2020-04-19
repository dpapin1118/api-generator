package com.globaljetlux.hubdb.service;

import com.globaljetlux.hubdb.config.Config;
import com.globaljetlux.hubdb.config.Definition;
import com.globaljetlux.hubdb.model.ModelInfo;
import com.globaljetlux.hubdb.model.RecordInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DtoBuilderService {


    @Autowired
    private NamingService namingService;

    public Map<String, List<ModelInfo>> generate(Definition def, List<ModelInfo> daoModel) {

        //Config conf = configService.getConfig();

        /**
         * We may know there is 1 pivot column or none.
         * This is where the column order is very important.
         */

        boolean hasPivot = def.pivot != null;

        // tripId is the pivot.

        var modelInfoMap = new HashMap<String, List<ModelInfo>>();
        var dtoModel1 = new ArrayList<ModelInfo>();
        var dtoModel2 = new ArrayList<ModelInfo>();
        boolean level1 = true;


        String dtoName = namingService.capitalize(def.model.name) + "Dto"; // "CrmTripDto", "OptionDto"

        String firstPivot = null;
        String subDtoName = null;
        if (hasPivot) {
            firstPivot = def.pivot.column; // tripId
            subDtoName = namingService.capitalize(def.pivot.name) + "Dto"; // TripDto
        }

        for (var f : daoModel) {

            if (hasPivot && f.getIdentifier().equals(firstPivot)) {

                ModelInfo dto = new ModelInfo();

                dto.setFieldUuid("");
                dto.setScope("private");
                dto.setType("List<"+ subDtoName +">");  // List<TripDto>
                dto.setIdentifier(def.pivot.name); // trip, optionValue
                dto.setJsonName("");
                dtoModel1.add(dto);

                level1 = false; // switch to level 2
            }

            ModelInfo dto = new ModelInfo();

            dto.setFieldUuid(f.getFieldUuid());
            dto.setScope(f.getScope());
            dto.setType(f.getType());
            dto.setIdentifier(f.getIdentifier());
            dto.setJsonName(namingService.dashCase(f.getIdentifier()));

            if ( level1 ) {
                dtoModel1.add(dto);
            } else {
                dtoModel2.add(dto);
            }

        }

        // Ordering field
        ModelInfo dto = new ModelInfo();
        dto.setFieldUuid("");
        dto.setScope("private");
        dto.setType("int");
        dto.setIdentifier("order");
        dto.setJsonName("");
        dtoModel1.add(dto);
        if (hasPivot) {
            dtoModel2.add(dto);
        }
        // ---

        modelInfoMap.put(dtoName, dtoModel1);
        if (!level1) {
            modelInfoMap.put(subDtoName, dtoModel2);
        }

        return modelInfoMap;
    }


}
