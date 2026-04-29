import java.util.List;

/**
 * Public class pager to simulate a "paged" list
 * 
 * AI USE DISCLOSURE:
 * This class was generated partially with AI. The only functions in which code was directly "copy-pasted" are the nextPage and previousPage functions
 * This was done because, while I had initially written the class myself, I could not get it to work properly
 * "flipping" pages often skipped items depending on whether the list size was odd or even, or would fail to change the page numbers correctly..
 * If you are really curious about my struggles, you can look at the git commit history...
 */
public class Pager<T> {

    public List<? extends T> list;
    
    private int pageSize;

    public int currentPage;

    private int current_start_index;

    private boolean atEnd = false;

    public Pager(List<? extends T> existingList, int pageSize) {
        this.list = existingList;
        this.pageSize = pageSize;
        this.currentPage = 0;
        this.current_start_index = 0;
        this.atEnd = existingList.isEmpty();
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /** Prints the current page */
    public void printCurrentPage() {
        if (list.size() == 0) {
            System.out.println("--- EMPTY LIST ---");
            return;
        }

        if (currentPage == list.size()/pageSize + 1) {
            nextPage();
            printCurrentPage();
            return;
        }

        System.out.printf("--- PAGE %d OF %d ---\n", currentPage+1, (int)Math.ceil((double)list.size()/pageSize));
        int end_index = ((current_start_index + pageSize) < list.size()) ? (current_start_index + pageSize) : list.size();
        //System.out.println(end_index); //debug
        
        for (int i = current_start_index; i < end_index; i++) {
            System.out.println(list.get(i));
        }
    }

    /** Increments page counter. Loops back to first page*/
    public void nextPage() {
        if (list == null || list.isEmpty()) {
            current_start_index = 0;
            currentPage = 0;
            return;
        }

        int totalPages = (int) Math.ceil((double) list.size() / pageSize);

        if (currentPage + 1 < totalPages) {
            currentPage++;
        } else {
            currentPage = 0;
        }

        current_start_index = currentPage * pageSize;
        }

    /** Decrements page counter */
    public void previousPage() {
        if (list.isEmpty()) {
            currentPage = 0;
            current_start_index = 0;
            return;
        }

        int totalPages = (int) Math.ceil((double) list.size() / pageSize);

        if (currentPage == 0) {
            currentPage = totalPages - 1;
        } else {
            currentPage--;
        }

        current_start_index = currentPage * pageSize;
    }

    //Sets it to the page or to the max page if it is too long
    public void setPage(int pageNum) {
        currentPage = (pageNum <= (list.size() / pageSize)) ? pageNum : ((list.size() / pageSize)-1);
        current_start_index = pageSize * currentPage;
    }

    /** Prints current page and then moves to next page */
    public void printAndAdvance() {
        printCurrentPage();
        nextPage();
    }

    /**
     * @return true if list is empty, false if not
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }
}