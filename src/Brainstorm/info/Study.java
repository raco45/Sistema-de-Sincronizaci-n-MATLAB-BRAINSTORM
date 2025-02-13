package brainstorm.info;

import com.mathworks.matlab.types.Struct;

/**
 *
 * @author raco1
 */
public class Study {

    public int studyIndex;
    public Struct study;
    public String subject;

    public Study(int index, Struct study, String subject) {
        this.studyIndex = index;
        this.study = study;
        this.subject = subject;
    }

    public String nombreStudy() {
        try {
            String name = (String) this.study.get("Name");
            return name;
        } catch (Exception e) {
            return null;
        }

    }

    public String fileNombreStudy() {
        try {
            String name = (String) this.study.get("FileName");
            return name;
        } catch (Exception e) {
            return null;
        }
    }
    public String dataFileName() {
        try {
            Struct data = (Struct) this.study.get("Data");
            String name= (String) data.get("FileName");
            return name;
        } catch (Exception e) {
            return null;
        }
    }
    
    public String dataVideoFileName(){
        try {
            Struct data = (Struct) this.study.get("Image");
            String name= (String) data.get("FileName");
            return name;
        } catch (Exception e) {
            return null;
        }
    }
    public String channelFileName(){
        try{
            Struct channel = (Struct) this.study.get("Channel");
            String name = (String) channel.get("FileName");
            return name;
        }catch(Exception e){
            return null;
        }
    }

    /**
     * @return the studyIndex
     */
    public int getStudyIndex() {
        return studyIndex;
    }

    /**
     * @param studyIndex the studyIndex to set
     */
    public void setStudyIndex(int studyIndex) {
        this.studyIndex = studyIndex;
    }

    /**
     * @return the study
     */
    public Struct getStudy() {
        return study;
    }

    /**
     * @param study the study to set
     */
    public void setStudy(Struct study) {
        this.study = study;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }
}
