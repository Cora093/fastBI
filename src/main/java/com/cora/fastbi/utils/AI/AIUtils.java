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
                "请根据这两部分内容，按照以下指定格式生成两部分内容（此外不要输出任何多余的开头、结尾、注释）\n" +
                "【【【\n" +
                "前端Echarts V5的option配置对象js代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释\n" +
                "【【【\n" +
                "{明确的数据分析结论（这部分字数多于60字 越详细越好），不要生成多余的注释}\n\n";
        return prompt;
    }
}
