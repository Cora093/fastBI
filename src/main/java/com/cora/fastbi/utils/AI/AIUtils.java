package com.cora.fastbi.utils.AI;

public class AIUtils {

    /**
     * 获取提示语
     *
     * @return
     */
    public static String getPrompt() {
        String prompt = "你是一个专业数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
                "分析需求：\n" +
                "{数据分析的需求或者目标}\n" +
                "原始数据：\n" +
                "{csv格式的原始数据，用','作为分隔符，用'\\n'作为换行符}\n" +
                "请根据以上内容，按照指定格式生成以下两部分内容（注意需要生成两部分内容）\n" +
                "第一部分：\n" +
                "{前端Echarts V5的option配置对象json代码(以option开头)，将数据进行可视化，不要生成任何注释}\n" +
                "第二部分：\n" +
                "{给出详细明确的数据分析结论，以及通过数据分析给出的建议，注意这部分不能省略}";
        return prompt;
    }
}
