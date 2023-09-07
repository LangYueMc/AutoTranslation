public class Test {
    public static void main(String[] args) {
        String t = """
                {
                  "neruina.kick.message": "aaa",
                  "enchantment.amethyst_imbuement.cleanse.noun"\u200B\u200B: "aaa",
                  "neruina.ticking.block_state": "aaa”,
                }
                """;
        System.out.println(t);
        System.out.println(t
                // key 前的
                .replaceAll("\r?\n +[“”]", "\n\"")
                // key 后
                .replaceAll("[\"“”][^\"“”:： ]*[:：] *", "\": ")
                // value 前
                .replaceAll("[:：] *[\"“”]", ": \"")
                // value 后
                .replaceAll("[\"“”][,，] *\r?\n", "\",\n")
                .replaceAll("[\"“”][^\"“”]*\r?\n *}", "\"\n}")
        );
    }
}
