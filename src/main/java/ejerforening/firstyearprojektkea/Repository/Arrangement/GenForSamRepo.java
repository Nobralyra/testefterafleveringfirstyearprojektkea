package ejerforening.firstyearprojektkea.Repository.Arrangement;
import ejerforening.firstyearprojektkea.Model.Arrangement.ArrangementOplysninger;
import ejerforening.firstyearprojektkea.Model.Arrangement.Generalforsamling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

/**
 * @author paivi
 * Repository for generalforsamlinger implementerer metoderne fra interface for generalforsamlinger.
 *
 */
@Repository
public class GenForSamRepo implements IGenForSamRepo {

    /**
     *  Klassen autowirer jdbctemplate, som den bruger i metoderne til at udfoere sql-statements.
     *  BeanPropertyRowMapper (interface er RowMapper) henter en raekke fra databasen som et object af den type,
     *  som den faar som parameter. Der er flere metoder, hvor rowmapperen til Generalforsamling bruges, saa der
     *  laves en faelles instans paa klasseniveuaet.
     *  Klassen faar ogsaa Arraylist af Integer som felt, fordi to metoder skal have adgang til den, saa dens scope
     *  skal vaere op klasseniveauet og ikke lokalt i en metode.
     */
    @Autowired
    JdbcTemplate jdbcTemplate;
    RowMapper rowmapper = new BeanPropertyRowMapper<>(Generalforsamling.class);
    ArrayList<Integer> arrOplysIderne;

    /**
     * Metoden henter alle kolonner fra generalforsamling og arrangement, hvor arrangementId har den samme vaerdi.
     * jdbcTemplate sender query til databasen og der returneres List med Generalforsamlinger.
     *
     * RowMapper har den begraensning, at man kun kan give den én klasse som parameter. Service har brug for ogsaa
     * at hente ArrangementOplysninger,som er knyttet til Arrangement, men jdbcTemplate kan ikke hente oplysninger
     * fra tre tabeller ad gangen.
     *
     * Saa der skal laves en ny metode til det: hentAlleArranOplysninger()- se laengere nede i klassen.
     * Den skal bruge vaerdierne i kolonnen arrOplysId fra dette soegeresultat, saa de gemmes i arraylisten arrOplysIderne.
     *
     * @return List, som indeholder referencer til instanserne af Generalforsamling.
     */

    public List<Generalforsamling> hentAlleGeneralforsamlinger() {
        String sql = "SELECT * FROM generalforsamling g, arrangement a WHERE g.arrangementId=a.arrangementId";
        List<Generalforsamling> genList = jdbcTemplate.query(sql, rowmapper);

        arrOplysIderne = new ArrayList<>();
        for (Generalforsamling g : genList) {
            arrOplysIderne.add(g.getArranOplysId());
        }
        return genList;
    }

    /**
     * Metoden konveterer foerst vaerdierne til String fra kolonnen arrOplysId fra soegeresultatet
     * i metoden hentAlleGeneralforsamlinger(). Disse vaerdier kan bruges i WHERE ..IN -clause.
     * Metoden laver beanPropertyMapper til ArrangementOplysninger (tabel- og klassenavnet).
     * Der hentes alle de raekker, hvor arrOplysId har den samme vaerdi som i det foerste soegresultat.
     * JdbcTemplate sender query til databasen og der returneres en List med elementerne.
     *
     * Normalt ville man goere dette med to joins, men fordi jdbctemplate ikke understoetter soegninger i flere end to
     * tabeller, er det dermed udfoert her ved at lave to metoder for sig.
     *
     * @return List, som indeholder referencer til instanserne af ArranegementOplysninger.
     */
    public List<ArrangementOplysninger> hentAlleArranOplysninger() {
        String vaerdier = "";
        if (arrOplysIderne.size() != 0) {
            //pga. stakitproblem
            vaerdier = String.valueOf(arrOplysIderne.get(0));
            if (arrOplysIderne.size() > 1) {
                for (int i = 1; i < arrOplysIderne.size(); i++) {
                    vaerdier = vaerdier + "," + String.valueOf(arrOplysIderne.get(i));
                }
            }
        }
        vaerdier = "(" + vaerdier + ")";
        RowMapper rowmapper = new BeanPropertyRowMapper<>(ArrangementOplysninger.class);
        String sql = "SELECT * FROM arrangementOplysninger WHERE arranOplysId IN" + vaerdier;
        return jdbcTemplate.query(sql, rowmapper);
    }




}
