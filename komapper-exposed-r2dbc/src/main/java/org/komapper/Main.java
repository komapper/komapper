package org.komapper;

//TIP コードを<b>実行</b>するには、<shortcut actionId="Run"/> を押すか
// ガターの <icon src="AllIcons.Actions.Execute"/> アイコンをクリックします。
public class Main {
    public static void main(String[] args) {
        //TIP ハイライトされたテキストにキャレットがある状態で <shortcut actionId="ShowIntentionActions"/> を押すと
        // IntelliJ IDEA によるその修正案を確認できます。
        System.out.printf("Hello and welcome!");

        for (int i = 1; i <= 5; i++) {
            //TIP <shortcut actionId="Debug"/> を押してコードのデバッグを開始します。<icon src="AllIcons.Debugger.Db_set_breakpoint"/> ブレークポイントを 1 つ設定しましたが、
            // <shortcut actionId="ToggleLineBreakpoint"/> を押すといつでも他のブレークポイントを追加できます。
            System.out.println("i = " + i);
        }
    }
}