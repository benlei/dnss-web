package dnss.tools.dnt;

import dnss.tools.commons.Accumulator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DNTAccumulator implements Accumulator<HashMap<String, Object>> {
    private Set<Map.Entry<String,DNT>> fields;

    public DNTAccumulator(String output) {
        // set file output
    }

    public void accumulate(Set<Map.Entry<String,DNT>> fields) {
        this.fields = fields;
        // accumulate the table creation
    }

    @Override
    public void accumulate(HashMap<String, Object> obj) {

    }

    @Override
    public void dissipate() {

    }
}
