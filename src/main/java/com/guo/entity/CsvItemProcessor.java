package com.guo.entity;

import org.springframework.batch.item.validator.ValidatingItemProcessor;

public class CsvItemProcessor extends ValidatingItemProcessor<Person>{
	@Override
    public Person process(Person item){

        super.process(item); // 需执行这句话，才会调用自定义的校验器

        if (item.getNation().equals("汉族")){
            item.setNation("01");
        }else{
            item.setNation("02");
        }
        return item;
    }
}
