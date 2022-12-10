package com.example.stepapp.persistence;

import android.content.Context;
import com.example.stepapp.persistence.model.DailySteps;
import java.util.List;

public class DailyStepsRemoteDaoService
  implements DailyStepsDaoService<DailySteps> {

  @Override
  public DailySteps get(Context context, String day) {
    return null;
  }

  @Override
  public long insert(Context context, DailySteps record) {
    return 0;
  }

  @Override
  public long update(Context context, DailySteps record) {
    return 0;
  }

  @Override
  public long delete(Context context, DailySteps record) {
    return 0;
  }
}
