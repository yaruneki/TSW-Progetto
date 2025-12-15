package control.maglietta;

import model.maglietta.MagliettaDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

public class DeleteMagliettaTest {

    private DeleteMaglietta servlet;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private ServletContext context;
    private RequestDispatcher dispatcherOk;
    private RequestDispatcher dispatcherError;

    @BeforeEach
    void setup() {
        servlet = new DeleteMaglietta();
        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        context = mock(ServletContext.class);
        dispatcherOk = mock(RequestDispatcher.class);
        dispatcherError = mock(RequestDispatcher.class);

        when(req.getServletContext()).thenReturn(context);
        when(context.getRealPath(anyString())).thenReturn("/tmp");

        when(req.getParameter("ID")).thenReturn("5");
        when(req.getParameter("tipo")).thenReturn("T");

        when(req.getRequestDispatcher("catalogoAdmin.jsp")).thenReturn(dispatcherOk);
        when(req.getRequestDispatcher("/pages/errorpage.jsp")).thenReturn(dispatcherError);
    }

    // -------- Test doPost() --------

    // {delete_true}
    @Test
    void doPost_deleteTrue() throws Exception {
        try (MockedConstruction<MagliettaDAO> mocked =
                     mockConstruction(MagliettaDAO.class,
                             (dao, ctx) -> when(dao.deleteMaglietta(5)).thenReturn(true))) {

            servlet.doPost(req, resp);

            verify(dispatcherOk).forward(req, resp);
            verify(dispatcherError, never()).forward(any(), any());
        }
    }

    // {delete_false}
    @Test
    void doPost_deleteFalse() throws Exception {
        try (MockedConstruction<MagliettaDAO> mocked =
                     mockConstruction(MagliettaDAO.class,
                             (dao, ctx) -> when(dao.deleteMaglietta(5)).thenReturn(false))) {

            servlet.doPost(req, resp);

            verify(dispatcherError).forward(req, resp);
            verify(dispatcherOk).forward(req, resp);
        }
    }

    // {delete_throws_SQLException}
    @Test
    void doPost_deleteThrowsSQLException() throws Exception {
        try (MockedConstruction<MagliettaDAO> mocked =
                     mockConstruction(MagliettaDAO.class,
                             (dao, ctx) -> when(dao.deleteMaglietta(5))
                                     .thenThrow(new java.sql.SQLException()))) {

            servlet.doPost(req, resp);

            verify(dispatcherError).forward(req, resp);
            verify(dispatcherOk).forward(req, resp);
        }
    }
}
