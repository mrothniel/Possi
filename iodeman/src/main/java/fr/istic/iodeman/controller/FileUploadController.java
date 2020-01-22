package fr.istic.iodeman.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import fr.istic.iodeman.SessionComponent;
import fr.istic.iodeman.model.Planning;
import fr.istic.iodeman.service.PlanningService;

@Controller
public class FileUploadController {

	@Autowired
	PlanningService planningService;
	
	@Autowired
	private SessionComponent session;
	
    @SuppressWarnings("finally")
	@RequestMapping(value="/upload", method=RequestMethod.POST)
    public String handleFileUpload(@RequestParam("planningId") int planningId, @RequestParam("file") MultipartFile inputFile, @RequestParam(value="redirectURL", required=false) String redirectURL){    	
    	
    	session.teacherOnly();
    	// path to save the input file
    	String name = "/tmp/"+new DateTime();
    	String nameCsv = "";
    	
    	// output file
    	File outputFile = new File(name);
    	
    	if (!inputFile.isEmpty()) {
            try {
            	nameCsv = inputFile.getOriginalFilename();

                byte[] bytes = inputFile.getBytes();
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(outputFile));
                stream.write(bytes);
                stream.close();
            } catch (Exception e) {
            	e.printStackTrace();
				return "redirect:/#/planning/"+planningId+"?import=nok";

			}
        }
    	
    	// on l'envoie au service
    	Planning planning = planningService.findById(new Integer(planningId));
    	try {
			planningService.importPartcipants(planning, outputFile);
			planningService.update(planning, planning.getName(), nameCsv, planning.getPeriod(), planning.getOralDefenseDuration(), planning.getOralDefenseInterlude(), planning.getLunchBreak(), planning.getDayPeriod(), planning.getNbMaxOralDefensePerDay(),planning.getRooms());
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/#/planning/"+planningId+"?import=nok";
			// affcher erreur de format
		}
    	
    	outputFile.delete();
    	
    	return "redirect:/#/planning/"+planningId+"?import=ok";

    }

}