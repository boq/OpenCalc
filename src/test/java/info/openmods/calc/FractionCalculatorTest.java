package info.openmods.calc;

import info.openmods.calc.CalcTestUtils.CalcCheck;
import info.openmods.calc.CalcTestUtils.SymbolStub;
import info.openmods.calc.executable.IExecutable;
import info.openmods.calc.types.fraction.FractionCalculatorFactory;
import org.apache.commons.lang3.math.Fraction;
import org.junit.Test;

public class FractionCalculatorTest {

	private final Calculator<Fraction, ExprType> sut = FractionCalculatorFactory.createDefault();

	public CalcCheck<Fraction> prefix(String value) {
		return CalcCheck.create(sut, value, ExprType.PREFIX);
	}

	public CalcCheck<Fraction> infix(String value) {
		return CalcCheck.create(sut, value, ExprType.INFIX);
	}

	public CalcCheck<Fraction> postfix(String value) {
		return CalcCheck.create(sut, value, ExprType.POSTFIX);
	}

	public CalcCheck<Fraction> compiled(IExecutable<Fraction> expr) {
		return CalcCheck.create(sut, expr);
	}

	public static Fraction f(int value) {
		return Fraction.getFraction(value, 1);
	}

	public static Fraction f(int numerator, int denominator) {
		return Fraction.getFraction(numerator, denominator);
	}

	@Test
	public void testBasicPrefix() {
		prefix("(+ 1 2)").expectResult(f(3));
		prefix("(* 2 3)").expectResult(f(6));
		prefix("(- 1)").expectResult(f(-1));
		prefix("(* (- 1) (+ 2 3))").expectResult(f(-5));
		prefix("(/ 10 2)").expectResult(f(5));

		prefix("(max 1)").expectResult(f(1));
		prefix("(max 1 2)").expectResult(f(2));
		prefix("(max 1 2 3)").expectResult(f(3));
	}

	@Test
	public void testBasicPostfix() {
		postfix("1 2 +").expectResult(f(3));
		postfix("2 3 *").expectResult(f(6));
		postfix("10 2 /").expectResult(f(5));
		postfix("1 2 / 1 2 / +").expectResult(f(1));
		postfix("1 2 / 1 +").expectResult(f(3, 2));
	}

	@Test
	public void testPostfixSymbols() {
		sut.environment.setGlobalSymbol("a", f(5));
		postfix("a").expectResult(f(5));
		postfix("a$0").expectResult(f(5));
		postfix("a$,1").expectResult(f(5));
		postfix("a$0,1").expectResult(f(5));
		postfix("@a").expectResult(f(5));
	}

	@Test(expected = RuntimeException.class)
	public void testPostfixFunctionGet() {
		postfix("@abs").execute();
	}

	@Test
	public void testPostfixStackOperations() {
		postfix("2 dup +").expectResult(f(4));
		postfix("1 2 3 pop +").expectResult(f(3));
		postfix("2 3 swap -").expectResult(f(1));
	}

	@Test
	public void testPostfixDupWithArgs() {
		postfix("0 1 2 dup$2").expectResults(f(0), f(1), f(2), f(1), f(2));
	}

	@Test
	public void testPostfixDupWithReturns() {
		postfix("1 2 dup$,4").expectResults(f(1), f(2), f(2), f(2), f(2));
	}

	@Test
	public void testPostfixDupWithArgsAndReturns() {
		postfix("1 2 3 4 dup$3,5").expectResults(f(1), f(2), f(3), f(4), f(2), f(3));
	}

	@Test
	public void testPostfixPopWithArgs() {
		postfix("1 2 3 4 pop$3").expectResults(f(1));
	}

	@Test
	public void testVariadicPostfixFunctions() {
		postfix("max$0").expectResult(f(0));
		postfix("1 max$1").expectResult(f(1));
		postfix("1 2 max$2").expectResult(f(2));

		postfix("3 2 1 sum$3").expectResult(f(6));
		postfix("3 2 1 avg$3").expectResult(f(2));
	}

	@Test
	public void testBasicInfix() {
		infix("1 + 2").expectResult(f(3));
		infix("2 * 3").expectResult(f(6));
		infix("10(2)").expectResult(f(20));
		infix("10 / 2").expectResult(f(5));
		infix("1/2 + 1/2").expectResult(f(1));
		infix("0.5 + 1/2").expectResult(f(1));
		infix("-3/2").expectResult(f(-3, 2));
	}

	@Test
	public void testBasicOrdering() {
		infix("1 + 2 - 3").expectResult(f(0));

		infix("1 + 2 * 3").expectResult(f(7));
		infix("1 + (2 * 3)").expectResult(f(7));
		infix("(1 + 2) * 3").expectResult(f(9));
		infix("-(1 + 2) * 3").expectResult(f(-9));
		infix("(1 + 2) * -3").expectResult(f(-9));
		infix("--3").expectResult(f(3));
	}

	@Test
	public void testBasicInfixFunctions() {
		infix("min(2,3)").expectResult(f(2));
		infix("max(2,3)").expectResult(f(3));
		infix("5max(2,3)").expectResult(f(15));
		infix("sqrt(9/4)").expectResult(f(3, 2));
		infix("2-max(2,3)").expectResult(f(-1));
		infix("int(7/3)").expectResult(f(2));
		infix("frac(7/3)").expectResult(f(1, 3));
		infix("numerator(7/3)").expectResult(f(7));
		infix("denominator(7/3)").expectResult(f(3));
	}

	@Test
	public void testVariadicInfixFunctions() {
		infix("max()").expectResult(f(0));
		infix("max(1)").expectResult(f(1));
		infix("max(1,2)").expectResult(f(2));
		infix("max(3,2,1)").expectResult(f(3));
	}

	@Test(expected = Exception.class)
	public void testTooManyParameters() {
		infix("abs(0, 1)").execute();
	}

	@Test
	public void testParserSwitch() {
		infix("2 + prefix(5)").expectResult(f(7));
		infix("2 + prefix((+ 5 6))").expectResult(f(13));

		prefix("(+ 2 (infix 5))").expectResult(f(7));
		prefix("(+ 2 (infix 5 + 6))").expectResult(f(13));
	}

	@Test
	public void testNestedParserSwitch() {
		infix("infix(5 + 2)").expectResult(f(7));
		infix("infix(infix(5 + 2))").expectResult(f(7));

		prefix("(prefix (+ 2 5))").expectResult(f(7));
		prefix("(prefix (prefix (+ 2 5)))").expectResult(f(7));

		infix("prefix((infix 2 + 5))").expectResult(f(7));
		prefix("(infix prefix((+ 2 5)))").expectResult(f(7));
	}

	@Test
	public void testConstantEvaluatingBrackets() {
		final SymbolStub<Fraction> stub = new SymbolStub<Fraction>()
				.allowCalls()
				.expectArgs(f(1), f(2))
				.verifyArgCount()
				.setReturns(f(5), f(6), f(7))
				.verifyReturnCount();
		sut.environment.setGlobalSymbol("dummy", stub);

		final IExecutable<Fraction> expr = sut.compilers.compile(ExprType.POSTFIX, "[1 2 dummy$2,3]");
		stub.checkCallCount(1);
		compiled(expr).expectResults(f(5), f(6), f(7));
		stub.checkCallCount(1);
	}

	@Test
	public void testNestedConstantEvaluatingBrackets() {
		final SymbolStub<Fraction> stub = new SymbolStub<Fraction>()
				.allowGets()
				.setGetValue(f(5));
		sut.environment.setGlobalSymbol("dummy", stub);

		final IExecutable<Fraction> expr = sut.compilers.compile(ExprType.POSTFIX, "[4 [@dummy 3 -] *]");
		stub.checkGetCount(1);
		compiled(expr).expectResults(f(8));
		stub.checkGetCount(1);
	}

	@Test
	public void testConstantEvaluatingSymbol() {
		final SymbolStub<Fraction> stub = new SymbolStub<Fraction>()
				.allowCalls()
				.expectArgs(f(1), f(2))
				.verifyArgCount()
				.setReturns(f(5))
				.verifyReturnCount();
		sut.environment.setGlobalSymbol("dummy", stub);

		final IExecutable<Fraction> expr = sut.compilers.compile(ExprType.INFIX, "9 + const(dummy(1,2) + 3)");
		stub.checkCallCount(1);

		compiled(expr).expectResults(f(17));
		stub.checkCallCount(1);

		compiled(expr).expectResults(f(17));
		stub.checkCallCount(1);
	}

	@Test
	public void testSimpleLet() {
		infix("let([x:2,y:3], x + y)").expectResult(f(5));
		infix("let([x=2,y=3], x + y)").expectResult(f(5));

		prefix("(let [(:x 2) (:y 3)] (+ x y))").expectResult(f(5));
		prefix("(let [(=x 2) (=y 3)] (+ x y))").expectResult(f(5));
	}

	@Test
	public void testLetWithExpression() {
		infix("let([x:1 + 2,y:3 + 4], x + y)").expectResult(f(10));
		prefix("(let [(:x (+ 1 2)) (:y (+ 3 4))] (+ x y))").expectResult(f(10));
	}

	@Test
	public void testNestedLet() {
		infix("let([x:2,y:3], let([w:x+y,z:x-y], w + z))").expectResult(f(4));
		prefix("(let [(:x 2) (:y 3)] (let [(:w (+ x y)) (:z (- x y))] (+ w z)))").expectResult(f(4));
	}

	@Test
	public void testFunctionLet() {
		infix("let([x():2], x())").expectResult(f(2));
		prefix("(let [(:(x) 2)] (x))").expectResult(f(2));

		infix("let([x(a,b):a+b], x(1,2))").expectResult(f(3));
		prefix("(let [(: (x a b) (+ a b))], (x 1 2))").expectResult(f(3));
	}

	@Test
	public void testLetScoping() {
		infix("let([x:2], let([y:x], let([x:3], y)))").expectResult(f(2));
		infix("let([x:2], let([f(a):a+x], let([x:3], f(4))))").expectResult(f(6));
		infix("let([x:5], let([x:2, y:x], x + y))").expectResult(f(7));
	}

	@Test
	public void testFailSymbol() {
		infix("fail('welp')").expectThrow(ExecutionErrorException.class, "welp");
		infix("fail()").expectThrow(ExecutionErrorException.class, null);
	}
}
