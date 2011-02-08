package com.funnelback.publicui.search.model.collection.paramtransform;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.funnelback.publicui.search.model.collection.paramtransform.criteria.Criteria;
import com.funnelback.publicui.search.model.collection.paramtransform.operation.Operation;

@RequiredArgsConstructor
public class Rule {
	@Getter private final Criteria criteria;
	@Getter private final List<Operation> operations;
	
	@Override
		public String toString() {
			StringBuffer out = new StringBuffer();
			out.append("\n-> IF ").append(criteria.toString()).append("\n");
			out.append("APPLY \n");
			for (Operation o: operations) {
				out.append("\t").append(o.toString()).append("\n");
			}
			out.append("<-\n");
			return out.toString();
		}
}

