package com.redhat.ceylon.js.repl;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.redhat.ceylon.compiler.js.DocVisitor;
import com.redhat.ceylon.compiler.js.JsCompiler;
import com.redhat.ceylon.compiler.typechecker.TypeChecker;
import com.redhat.ceylon.compiler.typechecker.TypeCheckerBuilder;
import com.redhat.ceylon.compiler.typechecker.context.PhasedUnit;
import com.redhat.ceylon.compiler.typechecker.parser.RecognitionError;
import com.redhat.ceylon.compiler.typechecker.tree.AnalysisMessage;
import com.redhat.ceylon.compiler.typechecker.tree.Message;
import com.redhat.ceylon.js.util.DocUtils;

/**
 * Servlet implementation class CeylonToJSTranslationServlet
 */
@WebServlet("/translate")
public class CeylonToJSTranslationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private SimpleJsonEncoder json = new SimpleJsonEncoder();

	/**
     * @see HttpServlet#HttpServlet()
     */
    public CeylonToJSTranslationServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    try {
    	    String script = request.getParameter("ceylon");
    	    ScriptFile src = new ScriptFile(script);
    	    //Run the typechecker
            TypeChecker typeChecker = new TypeCheckerBuilder()
                    .addSrcDirectory(src)
                    .getTypeChecker();
            typeChecker.process();
            //While we're at it, get the docs
            final DocVisitor doccer = new DocVisitor();
            for (PhasedUnit pu: typeChecker.getPhasedUnits().getPhasedUnits()) {
                pu.getCompilationUnit().visit(doccer);
            }
            //Run the compiler, if typechecker returns no errors.
            final CharArrayWriter out = new CharArrayWriter();
            JsCompiler compiler = new JsCompiler(typeChecker) {
                @Override
                protected Writer getWriter(PhasedUnit pu) {
                    return out;
                }
            }.optimize(true).stopOnErrors(true).indent(false).comment(false);
            boolean ok = compiler.generate();
            out.flush();
            out.close();
            final Map<String, Object> resp = new HashMap<String, Object>();
            resp.put("code_docs", doccer.getDocs());
            resp.put("code_refs", DocUtils.referenceMapToList(doccer.getLocations()));
            PrintWriter writer = response.getWriter();
            if (ok) {
                resp.put("code", out.toString());
            } else {
                //Print out errors
                ArrayList<Object> errs = new ArrayList<Object>(compiler.listErrors().size());
                for (Message err : compiler.listErrors()) {
                    errs.add(encodeError(err));
                }
                resp.put("errors", errs);
            }
            String enc = json.encode(resp);
            response.setContentLength(enc.length());
            writer.print(enc);
            writer.flush();
	    } catch (Exception ex) {
            response.setStatus(500);
            StringBuilder sb = new StringBuilder();
            json.encodeList(Collections.singletonList((Object)String.format("Service error: %s", ex.getMessage())), sb);
            response.setContentLength(sb.length());
            response.getWriter().print(sb.toString());
            response.getWriter().flush();
	    }
	}

    private Map<String, Object> encodeError(Message err) {
        Map<String, Object> errmsg = new HashMap<String, Object>();
        errmsg.put("msg", err.getMessage());
        errmsg.put("code", err.getCode());
        if (err instanceof AnalysisMessage) {
            AnalysisMessage msg = (AnalysisMessage)err;
            errmsg.putAll(DocUtils.locationToMap(msg.getTreeNode().getLocation(), true));
        } else if (err instanceof RecognitionError) {
            RecognitionError rec = (RecognitionError)err;
            String pos = String.format("%d:%d", rec.getLine(), rec.getCharacterInLine());
            errmsg.putAll(DocUtils.locationToMap(pos, true));
        }
        return errmsg;
    }

}
