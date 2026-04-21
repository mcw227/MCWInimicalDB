import java.util.Collection;

public class PagedList<T> extends ArrayList<T> {
    
    private int pageSize;

    public int currentPage;

    private int current_start_index;

    private boolean atEnd = false;

    public PagedList(Collection<? extends T> existingList, int pageSize) {
        super(existingList);
        this.pageSize = pageSize;
        this.currentPage = 0;
        this.current_start_index = 0;
        this.atEnd = this.isEmpty();
    }

    public setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /** Prints the current page */
    public void printCurrentPage() {
        if (this.size() == 0) {
            System.out.println("List is empty");
        }

        if (currentPage == this.size()/pageSize) {
            System.out.println("--- END OF LIST ---");
        }

        System.out.printf("--- PAGE %d OF %d ---", currentPage, this.size()/pageSize)
        int end_index = ((current_start_index + pageSize) < this.size()) ? (current_start_index + pageSize) : this.size()-1;
        System.out.println(end_index); //debug
        
        for (int i = current_start_index; i <= end_index; i++) {
            System.out.println(this.get(i));
        }

        current_start_index = end_index;
    }

    /** Increments page counter. Loops back to first page*/
    public void nextPage() {
        if (this.size() == 0) {
            current_start_index = 0;
            return;
        }

        if (current_start_index < this.size()) {
            int temp = current_start_index;
            current_start_index = ((current_start_index + pageSize) < this.size()) ? (current_start_index + pageSize) : 0;

            if (current_start_index == 0) {
                currentPage = 0;
            } else {
                currentPage++;
            }
        }

        else {
            current_start_index = 0;
            currentPage = 0;
        }
    }

    /** Decrements page counter */
    public void previousPage() {
        if (this.size() == 0) {
            current_start_index = 0;
            return;
        }

        if (current_start_index < this.size()) {
            int temp = current_start_index;
            current_start_index = ((current_start_index - pageSize) < 0) ? 0 : (current_start_index - pageSize);

            if (current_start_index != temp) {
                currentPage++;
            }
        }

        else {
            current_start_index = 0;
            currentPage = 0;
        }
    }

    //Sets it to the page or to the max page if it is too long
    public void setPage(int pageNum) {
        currentPage = (pageNum <= (this.size() / pageSize)) ? pageNum : ((this.size() / pageSize)-1);
        current_start_index = pageSize * currentPage;
    }

    /** Prints current page and then moves to next page */
    public void printAndAdvance() {
        printCurrentPage();
        nextPage();
    }
}