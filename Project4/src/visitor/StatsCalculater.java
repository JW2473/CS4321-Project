package visitor;

import java.util.List;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import util.Catalog;
import util.Tools;
import util.Catalog.statsInfo;

public class StatsCalculater implements ExpressionVisitor{
	int v;
	int reduction = 1;
	statsInfo s1;
	statsInfo s2;

	List<String> Schema1;
	List<String> Schema2;
	public void set(statsInfo s1, List<String> Schema1, statsInfo s2, List<String> Schema2) {
		this.s1 = s1;
		this.s2 = s2;
		this.Schema1 = Schema1;
		this.Schema2 = Schema2;
	}
	
	public void set(statsInfo s1, List<String> Schema1) {
		this.s1 = s1;
		this.Schema1 = Schema1;
		this.s2 = null;
		this.Schema2 = null;
	}
	
	public void visit(NullValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Function arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(InverseExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(JdbcParameter arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DoubleValue arg0) {

	}

	@Override
	public void visit(LongValue arg0) {
		this.v = Integer.parseInt(arg0.toString());
	}

	@Override
	public void visit(DateValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimeValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimestampValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Parenthesis arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(StringValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Addition arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Division arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Multiplication arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Subtraction arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AndExpression arg0) {
		arg0.getLeftExpression().accept(this);
		statsInfo st1 = Catalog.getInstance().new statsInfo(s1);
		statsInfo st2 = Catalog.getInstance().new statsInfo(s2);
		int reduction_left = reduction;
		arg0.getRightExpression().accept(this);
		for(int i = 0; i < st1.ma.length; i++) {
			int ma_new = st1.ma[i]>s1.ma[i]? s1.ma[i]: st1.ma[i];
			int mi_new = st1.mi[i]<s1.mi[i]? s1.mi[i]: st1.mi[i];
			if(mi_new > ma_new) {
				ma_new = 0;
				mi_new = 0;
			}
			//s1.n = s1.n*(ma_new - mi_new + 1)/(s1.ma[i] - s1.mi[i] + 1);
			s1.ma[i] = ma_new;
			s1.mi[i] = mi_new;
		}
		if(s2 != null)
			for(int i = 0; i < st2.ma.length; i++) {
				int ma_new = st2.ma[i]>s2.ma[i]? s2.ma[i]: st2.ma[i];
				int mi_new = st2.mi[i]<s2.mi[i]? s2.mi[i]: st2.mi[i];
				if(mi_new > ma_new) {
					ma_new = 0;
					mi_new = 0;
				}
				//s2.n = s2.n*(ma_new - mi_new + 1)/(s2.ma[i] - s2.mi[i] + 1);
				s2.ma[i] = ma_new;
				s2.mi[i] = mi_new;
			}
		reduction = reduction_left*reduction;
	}

	@Override
	public void visit(OrExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Between arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(EqualsTo arg0) {
		boolean lc = arg0.getLeftExpression() instanceof Column;
		boolean rc = arg0.getRightExpression() instanceof Column;
		if(lc & rc) {
			arg0.getLeftExpression().accept(this);
			int v1 = v;
			arg0.getRightExpression().accept(this);
			statsInfo st1 = s1;
			statsInfo st2 = s1;
			if(v1 >= 1000) {
				st1 = s2;
				v1 -= 1000;
			}
			if(v >= 1000) {
				st2 = s2;
				v -= 1000;
			}
			int reduction1 = st1.ma[v1] - st1.mi[v1] + 1 > st1.n? st1.n: st1.ma[v1] - st1.mi[v1] + 1;
			int reduction2 = st2.ma[v] - st2.mi[v] + 1 > st2.n? st2.n: st2.ma[v] - st2.mi[v] + 1;
			reduction = reduction1 > reduction2? reduction1: reduction2;			
			st1.ma[v1] = st1.ma[v1]>st2.ma[v]? st2.ma[v]: st1.ma[v1];
			st1.mi[v1] = st1.mi[v1]<st2.mi[v]? st2.mi[v]: st1.mi[v1];
			if(st1.mi[v1] > st1.ma[v1]) {
				st1.ma[v1] = 0;
				st1.mi[v1] = 0;
			}
			st2.ma[v] = st1.ma[v1];
			st2.mi[v] = st1.mi[v1];
		}
		else if(lc) {
			arg0.getLeftExpression().accept(this);
			int v1 = v;
			arg0.getRightExpression().accept(this);
			if(v1 >= 1000) {
				//s2.n = s2.n / (s2.ma[v1-1000] - s2.mi[v1-1000] + 1);
				s2.ma[v1-1000] = v;
				s2.mi[v1-1000] = v;
			}
			else {
				//s1.n = s1.n / (s1.ma[v1] - s2.mi[v1] + 1);
				s1.ma[v1] = v;
				s1.mi[v1] = v;
			}
		}
		else {
			arg0.getRightExpression().accept(this);
			int v1 = v;
			arg0.getLeftExpression().accept(this);
			if(v1 >= 1000) {
				//s2.n = s2.n / (s2.ma[v1-1000] - s2.mi[v1-1000] + 1);
				s2.ma[v1-1000] = v;
				s2.mi[v1-1000] = v;
			}
			else {
				//s1.n = s1.n / (s1.ma[v1] - s2.mi[v1] + 1);
				s1.ma[v1] = v;
				s1.mi[v1] = v;
			}
		}
	}

	@Override
	public void visit(GreaterThan arg0) {
		boolean lc = arg0.getLeftExpression() instanceof Column;
		boolean rc = arg0.getRightExpression() instanceof Column;
		if(lc & rc) {
			arg0.getLeftExpression().accept(this);
			int v1 = v;
			arg0.getRightExpression().accept(this);
			statsInfo st1 = s1;
			statsInfo st2 = s1;
			if(v1 >= 1000) {
				st1 = s2;
				v1 -= 1000;
			}
			if(v >= 1000) {
				st2 = s2;
				v -= 1000;
			}
			st1.mi[v1] = st1.mi[v1]<st2.mi[v]+1? st2.mi[v]+1: st1.mi[v1];
			if(st1.mi[v1] > st1.ma[v1]) {
				st1.ma[v1] = 0;
				st1.mi[v1] = 0;
			}
			st2.ma[v] = st1.ma[v1]-1>st2.ma[v]? st2.ma[v]: st1.ma[v1]-1;
			if(st2.mi[v] > st2.ma[v]) {
				st2.ma[v] = 0;
				st2.mi[v] = 0;
			}
		}
		else if(lc) {
			arg0.getLeftExpression().accept(this);
			int v1 = v;
			arg0.getRightExpression().accept(this);
			statsInfo st1 = s1;
			if(v1 >= 1000) {
				st1 = s2;
				v1 -= 1000;
			}
			st1.mi[v1] = st1.mi[v1]<v+1? v+1: st1.mi[v1];
			if(st1.mi[v1] > st1.ma[v1]) {
				st1.ma[v1] = 0;
				st1.mi[v1] = 0;
			}
		}
		else {
			arg0.getLeftExpression().accept(this);
			int v1 = v;
			arg0.getRightExpression().accept(this);
			statsInfo st2 = s1;
			if(v >= 1000) {
				st2 = s2;
				v -= 1000;
			}
			st2.ma[v] = st2.ma[v]>v1-1? v1-1: st2.ma[v];
			if(st2.mi[v] > st2.ma[v]) {
				st2.ma[v] = 0;
				st2.mi[v] = 0;
			}
		}
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(InExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IsNullExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LikeExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MinorThan arg0) {
		boolean lc = arg0.getLeftExpression() instanceof Column;
		boolean rc = arg0.getRightExpression() instanceof Column;
		if(lc & rc) {
			arg0.getLeftExpression().accept(this);
			int v1 = v;
			arg0.getRightExpression().accept(this);
			statsInfo st1 = s1;
			statsInfo st2 = s1;
			if(v1 >= 1000) {
				st1 = s2;
				v1 -= 1000;
			}
			if(v >= 1000) {
				st2 = s2;
				v -= 1000;
			}
			st1.ma[v1] = st1.ma[v1]>st2.ma[v]-1? st2.ma[v]-1: st1.ma[v1];
			if(st1.mi[v1] > st1.ma[v1]) {
				st1.ma[v1] = 0;
				st1.mi[v1] = 0;
			}
			st2.mi[v] = st1.mi[v1]+1<st2.mi[v]? st2.mi[v]: st1.mi[v1]+1;
			if(st2.mi[v] > st2.ma[v]) {
				st2.ma[v] = 0;
				st2.mi[v] = 0;
			}
		}
		else if(lc) {
			arg0.getLeftExpression().accept(this);
			int v1 = v;
			arg0.getRightExpression().accept(this);
			statsInfo st1 = s1;
			if(v1 >= 1000) {
				st1 = s2;
				v1 -= 1000;
			}
			st1.ma[v1] = st1.ma[v1]>v-1? v-1: st1.ma[v1];
			if(st1.mi[v1] > st1.ma[v1]) {
				st1.ma[v1] = 0;
				st1.mi[v1] = 0;
			}
		}
		else {
			arg0.getLeftExpression().accept(this);
			int v1 = v;
			arg0.getRightExpression().accept(this);
			statsInfo st2 = s1;
			if(v >= 1000) {
				st2 = s2;
				v -= 1000;
			}
			st2.mi[v] = st2.ma[v]<v1+1? v1+1: st2.ma[v];
			if(st2.mi[v] > st2.ma[v]) {
				st2.ma[v] = 0;
				st2.mi[v] = 0;
			}
		}
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Column arg0) {
		String s = Tools.rebuildWholeColumnName(arg0);
		if(Schema1.contains(s))
			v = Schema1.indexOf(arg0.getColumnName().toString());
		else 
			v = 1000 + Schema2.indexOf(arg0.getColumnName().toString());
	}

	@Override
	public void visit(SubSelect arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CaseExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WhenClause arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ExistsExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AllComparisonExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AnyComparisonExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Concat arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Matches arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseAnd arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseOr arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseXor arg0) {
		// TODO Auto-generated method stub
		
	}

}
