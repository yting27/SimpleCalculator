package calculator;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Font;
import java.util.EmptyStackException;
import java.util.Stack;


public class Controller{
    private final String[][] buttonStr = {
            {"C", "+/-", "^", "DEL"},
            {"7", "8", "9" , "/"},
            {"4", "5", "6", "*"},
            {"1", "2", "3", "-"},
            {"0", ".", "=", "+"}
    };

    String expression;
    String input;
    Stack<Double> numericStack;
    Stack<Character> operatorStack;
    double lastValue;

    @FXML
    private Label previous;

    @FXML
    private Label current;

    @FXML
    private TilePane tilePane;

    @FXML
    public void initialize(){
        input = "";
        expression = "";
        numericStack = new Stack<>();
        operatorStack = new Stack<>();
        lastValue = 0;

        previous.setText("");
        current.setText("");

        for(int i = 0; i < buttonStr.length; i++){
            for(int j = 0; j < buttonStr[0].length; j++){
                Button button = new Button(buttonStr[i][j]);
                button.setPrefSize(50, 40);
                button.setFont(new Font(13));
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String text = ((Button) event.getSource()).getText();
                        // If user pressed "=" button, expression will be evaluated.
                        if(text.compareTo("=") == 0){
                            if(expression.length() > 0)
                                if(Character.isDigit(input.charAt(input.length() - 1)) ||
                                        input.charAt(input.length() - 1) == '.') {
                                    evaluateExpression();
                                } else {
                                    triggerAlert();
                                }
                            return;
                        }

                        // If user pressed "DEL" button.
                        if(text.compareTo("DEL") == 0){
                            if(input.length() > 0) {
                                input = input.substring(0, input.length() - 1);
                                if (expression.charAt(expression.length() - 1) == '#') {
                                    expression = expression.substring(0, expression.length() - 3);
                                } else {
                                    expression = expression.substring(0, expression.length() - 1);
                                }
                            }

                        }
                        // If user pressed "+/-" button, he/she intended to enter negative
                        // or de-negative(aka "positive" - if he/she accidentally pressed the button right before)
                        else if(text.compareTo("+/-") == 0){
                            if(expression.length() > 1 && expression.charAt(expression.length() - 1) == '-'
                                    && expression.charAt(expression.length() - 2) == '#'){
                                input = input.substring(0, input.length() - 1);
                                expression = expression.substring(0, expression.length() - 1);
                            }else if(expression.length() > 0 && expression.charAt(expression.length() - 1) == '#'){
                                input += "-";
                                expression += "-";
                            } else if(expression.length() == 0){
                                input += "-";
                                expression += "-";
                            } else if(expression.length() == 1 && expression.charAt(0) == '-'){
                                input = "";
                                expression = "";
                            }
                        }
                        // User intended to clear the whole expression entered.
                        else if(text.compareTo("C") == 0){
                            input = "";
                            expression = "";
                        }

                        // If user pressed operator button, we will insert "#" before and after the symbol to facilitate
                        // expression evaluation process.
                        else if(text.compareTo("+") == 0|| text.compareTo("-") == 0 ||
                                    text.compareTo("*") == 0 || text.compareTo("/") == 0 || text.compareTo("^") == 0) {
                            if (expression.length() > 0){
                                if (expression.charAt(expression.length() - 1) == '#') {
                                    input = input.substring(0, input.length() - 1) + text;
                                    expression = expression.substring(0, expression.length() - 2) + text + "#";
                                } else {
                                    input += text;
                                    expression += "#" + text + "#";
                                }
                            } else{
                                expression = Double.toString(lastValue) + "#" + text + "#";
                                input = Double.toString(lastValue) + text;
                            }
                        }
                        else{
                            // User are not allowed to enter number with two decimal points sine it doesn't make sense.
                            if(text.compareTo(".") == 0){
                                int i = expression.length() - 1;
                                boolean decimal = false;
                                while(i >= 0 && expression.charAt(i) != '#'){
                                    if(expression.charAt(i) == '.') {
                                        decimal = true;
                                        break;
                                    }
                                    i--;
                                }
                                if(decimal) {
                                    triggerAlert();
                                    return;
                                }
                            }

                            input += text;
                            expression += text;
                        }

                        current.setText(input);
                    };
                });
                tilePane.getChildren().add(button);
            }
        }
    }

    public void evaluateExpression() {
        String[] exp = expression.split("#");

//        for (int i = 0; i < exp.length; i++) {
//            System.out.println(exp[i] + " ");
//        }

        for (int i = 0; i < exp.length; i++) {
            if (exp[i].length() == 1 && !Character.isDigit(exp[i].charAt(0)) && exp[i].charAt(0) != '.') {
                if (!operatorStack.isEmpty()) {
                    boolean repushPrevious = true;
                    char previousOp = operatorStack.pop();
                    while (higherOrEqualPrecedence(previousOp, exp[i].charAt(0))) {
                        double op2 = numericStack.pop();
                        double op1 = numericStack.pop();
                        switch (previousOp) {
                            case '+':
                                numericStack.push(op1 + op2);
                                break;
                            case '-':
                                numericStack.push(op1 - op2);
                                break;
                            case '*':
                                numericStack.push(op1 * op2);
                                break;
                            case '/':
                                numericStack.push(op1 / op2);
                                break;
                            case '^':
                                numericStack.push(Math.pow(op1, op2));
                                break;
                        }
                        if (!operatorStack.empty())
                            previousOp = operatorStack.pop();
                        else {
                            repushPrevious = false;
                            break;
                        }
                    }

                    // Repush the operator popped from the operatorStack into the stack.
                    if (repushPrevious) {
                        operatorStack.push(previousOp);
                    }
                }
                operatorStack.push(exp[i].charAt(0));

            } else {
                double value;
                try {
                    // Here we regard a standalone "." as 0.0 but since the function Double.parseDouble() unable to convert "."
                    // in the way we desired, the if statement below is necessary to prevent any error from ocurring.
                    if (exp[i].length() == 1 && exp[i].charAt(0) == '.')
                        exp[i] += '0';

                    value = Double.parseDouble(exp[i]);
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                    break;
                }
                numericStack.push(value);
            }
        }

        while(!operatorStack.empty()) {
            double op2 = numericStack.pop();
            double op1 = numericStack.pop();

            char operator = operatorStack.pop();
            switch (operator) {
                case '+':
                    numericStack.push(op1 + op2);
                    break;
                case '-':
                    numericStack.push(op1 - op2);
                    break;
                case '*':
                    numericStack.push(op1 * op2);
                    break;
                case '/':
                    numericStack.push(op1 / op2);
                    break;
                case '^':
                    numericStack.push(Math.pow(op1, op2));
                    break;
            }
        }

        double result;
        try {
            result = numericStack.pop();
        } catch (EmptyStackException ex) {
            ex.printStackTrace();
            result = 0;
        }

        previous.setText(input);
        current.setText(Double.toString(result));
        lastValue = result;

        input = "";
        expression = "";

        numericStack.clear();
        operatorStack.clear();
    }

    public void triggerAlert(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Expression");
        alert.setHeaderText("Please ensure the expression is valid");
        alert.showAndWait();
    }

    public boolean higherOrEqualPrecedence(char a, char b){
        if(a == '^')
            return true;
        else if(b == '^')
            return false;
        else if(a == '*' || a == '/')
            return true;
        else if(b == '*' || b == '/')
            return false;
        else
            return true;
    }
}
